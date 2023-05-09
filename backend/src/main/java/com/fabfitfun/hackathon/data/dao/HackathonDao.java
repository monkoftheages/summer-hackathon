package com.fabfitfun.hackathon.data.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface HackathonDao extends SqlObject {

  @SqlQuery("SELECT id FROM hackathon")
  List<Integer> getAllIds();
}

