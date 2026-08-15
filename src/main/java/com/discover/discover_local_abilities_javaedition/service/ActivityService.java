package com.discover.discover_local_abilities_javaedition.service;

import java.util.List;

import com.discover.discover_local_abilities_javaedition.dto.ActivityWithHoursDTO;
import com.discover.discover_local_abilities_javaedition.model.WorkingHours;

public interface ActivityService {
    List<ActivityWithHoursDTO> findAll(String category, Double minRating, Integer minRatingCount);
    ActivityWithHoursDTO findByIndex(Long id);
    List<WorkingHours> listWorkingHoursOfActivity(Long id);
    List<ActivityWithHoursDTO> findNearby(Double lat, Double lon, Double radiusKm,String category, Double minRating, Integer minRatingCount);

//    List<Activity> findAllWithWorkingHours();
}
