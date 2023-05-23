package com.fabfitfun.hackathon.api.app.kafka;


import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Utils;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A callback interface where we implement custom actions when
 * the set of partitions assigned to the consumer changes.
 * @param <T>
 */
@JBossLog
@AllArgsConstructor
public class RebalanceListener<T> implements ConsumerRebalanceListener {

  private final KafkaConsumer<String, T> consumer;
  private final Map<TopicPartition, OffsetAndMetadata> offsetsToCommit;

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    log.warn("REBALANCE: Revoking partitions. Manually commit processed offsets.");
    if (offsetsToCommit != null && !offsetsToCommit.isEmpty()) {
      final Map<TopicPartition, OffsetAndMetadata> offsets =
          getTopicPartitionOffsetAndMetadataMap(partitions);
      if (!offsets.isEmpty()) {
        consumer.commitSync(offsets);
      }
    }
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    // start consuming from newly assigned partitions
    log.infof("REBALANCE: Assigned new partitions [%s]. Continue processing messages.",
        Utils.join(partitions, ", "));
  }

  /**
   * By default, and to maintain compatibility with pre-2.4.0 consumer applications, the
   * onPartitionsLost() callbacks simply delegates to onPartitionsRevoked(). However, the roles
   * of these callback methods are vastly different. When upgrading a consumer application that
   * implements the ConsumerRebalanceListener callback to utilize 2.4.0 (or newer) of the client
   * library, one should override the default implementation.
   * At this point, another consumer is already consuming from these lost partitions. That is not
   * a problem since the processing is idempotent.
   * @param lostPartitions
   */
  @Override
  public void onPartitionsLost(Collection<TopicPartition> lostPartitions) {
    // lost assigned partitions
    log.infof("REBALANCE: Lost previously assigned partitions [%s]",
        Utils.join(lostPartitions, ", "));
    if (offsetsToCommit != null && !offsetsToCommit.isEmpty()) {
      final Map<TopicPartition, OffsetAndMetadata> offsets =
          getTopicPartitionOffsetAndMetadataMap(lostPartitions);
      if (!offsets.isEmpty()) {
        for (OffsetAndMetadata c : offsets.values()) {
          log.infof("Message not committed with offset: %d", c.offset());
        }
      }
    }
  }

  /** Gets the partitions offsets map **/
  private Map<TopicPartition, OffsetAndMetadata> getTopicPartitionOffsetAndMetadataMap
  (Collection<TopicPartition> partitions) {
    return offsetsToCommit.entrySet().stream()
        .filter(map -> partitions.contains(map.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
