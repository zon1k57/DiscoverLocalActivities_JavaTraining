package com.discover.discover_local_abilities_javaedition.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.discover.discover_local_abilities_javaedition.dto.RecommendationsDTO;
import com.discover.discover_local_abilities_javaedition.service.RecommendationService;

@RestController
@RequestMapping(value="/api/recommendation")
public class RecommendationController{
  private final RecommendationService recommendationService;

  public RecommendationController(RecommendationService recommendationService){
    this.recommendationService = recommendationService;
  }

  @GetMapping("/{id}")
  public List<RecommendationsDTO> findByUserId(@PathVariable Long id,
                                                @RequestParam(required=false) Double radiusKm,
                                                @RequestParam(required=false) String context){
    return recommendationService.findByUserId(id, radiusKm, context);
  }
}
