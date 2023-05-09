package com.fabfitfun.hackathon.biz.manager;

import com.fabfitfun.hackathon.biz.service.HackathonService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HackathonManager {
  private final HackathonService hackathonService;

  public void manageData() {
    hackathonService.manageData();
  }
}
