package com.discover.discover_local_abilities_javaedition.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RecommendationsDTO {
    private Long userId;
    private Map<String,Double> userLocation;
    private Double radiusKm;
    private String context;
    private LocalDateTime responseTimestamp;
    private Integer resultCount;
    private List<ActivityWithHoursDTO> activities;
}
