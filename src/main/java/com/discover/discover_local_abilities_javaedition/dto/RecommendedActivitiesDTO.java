package com.discover.discover_local_abilities_javaedition.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class RecommendedActivitiesDTO{
    private Long activityId;
    private String activityName;
    private String activityType;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer userRatingCount;
    private Double distanceKm;
    private Double recommendedScore;
    private String categoryRelevance;
    private Boolean isOpen;
    private LocalDateTime responseStamp;
}
