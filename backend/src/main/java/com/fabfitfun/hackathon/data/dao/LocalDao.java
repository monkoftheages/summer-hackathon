package com.fabfitfun.hackathon.data.dao;

import com.fabfitfun.hackathon.data.Question;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface LocalDao extends SqlObject {

  @SqlQuery("SELECT s.shopUserId "
      + "FROM `hackathon_sentiment` s  "
      + "WHERE s.id = :id AND s.level >= :minimumSentimentLevel")
  public List<Long> getUserIds(@Bind("questionId") String questionId, @Bind("minimumSentimentLevel") int minimumSentimentLevel);

  @SqlQuery("SELECT * "
      + "FROM `hackathon_questions` q  ")
  @RegisterBeanMapper(Question.class)
  public List<Question> getQuestions();

  @SqlQuery("SELECT * "
      + "FROM `hackathon_questions` q  "
      + "WHERE q.id = :questionId ")
  @RegisterBeanMapper(Question.class)
  public Question getQuestion(@Bind("questionId") String questionId);

  @SqlQuery("SELECT SUM(s.level) "
      + "FROM `hackathon_sentiment` s  "
      + "WHERE s.questionId = :questionId ")
  public int getTotalSentiment(@Bind("questionId") String questionId);

  @SqlQuery("SELECT SUM(s.level) "
      + "FROM `hackathon_sentiment` s  "
      + "WHERE s.questionId = :questionId AND s.level > 75")
  public int getTotalHighSentiment(@Bind("questionId") String questionId);

  @SqlQuery("INSERT INTO hackathon_questions (query, total) "
      + "VALUES (:query, :totalCount)  ")
  public int insertQueryQuestion(@Bind("query") String query, @Bind("totalCount") int totalCount);

  @SqlQuery("INSERT INTO hackathon_sentiment (questionId, shopUserId, level) "
      + "VALUES (:questionId, :shopUserId, :level)  ")
  @GetGeneratedKeys
  int insertUserSentiment(@Bind("questionId") String questionId, @Bind("shopUserId") long shopUserId, @Bind("level") int level);
}