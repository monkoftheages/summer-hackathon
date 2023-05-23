package com.fabfitfun.hackathon.api.resource;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SentimentAnalysisResource {
    private Long userId;
    private String queryString;

}
