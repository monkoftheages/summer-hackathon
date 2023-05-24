package com.fabfitfun.hackathon.data.dao;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface LocalDao extends SqlObject {

  @SqlQuery("SELECT s.shopUserId "
      + "FROM `sentiment` s  "
      + "WHERE s.id = :id AND s.level >= :minimumSentimentLevel")
  public List<Long> getUserIds(@Bind("questionId") String questionId, @Bind("minimumSentimentLevel") int minimumSentimentLevel);


}