package com.fabfitfun.hackathon.data.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
public class SentimentResource {
    @BsonId
    private ObjectId id;
    @BsonProperty(value = "user_id")
    private String userId;
    private String keyword;
    private int sentiment;
}
