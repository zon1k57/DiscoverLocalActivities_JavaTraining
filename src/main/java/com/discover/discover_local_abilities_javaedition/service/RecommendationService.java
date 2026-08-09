package com.discover.discover_local_abilities_javaedition.service;

import java.util.List;

import com.discover.discover_local_abilities_javaedition.dto.RecommendationsDTO;

public interface RecommendationService {
    List<RecommendationsDTO> findById(Long id);
    List<RecommendationsDTO> findByChoords(Double lat, Double lon, Double radiusKm, String context);
}
