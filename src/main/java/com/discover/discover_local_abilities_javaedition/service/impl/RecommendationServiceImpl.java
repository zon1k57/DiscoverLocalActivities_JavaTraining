package com.discover.discover_local_abilities_javaedition.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.discover.discover_local_abilities_javaedition.dto.RecommendationsDTO;
import com.discover.discover_local_abilities_javaedition.repository.ActivityRepository;
import com.discover.discover_local_abilities_javaedition.repository.UserRepository;
import com.discover.discover_local_abilities_javaedition.repository.WorkingHoursRepository;
import com.discover.discover_local_abilities_javaedition.service.RecommendationService;

@Service
class RecommendationServiceImpl implements RecommendationService {
  private final ActivityRepository activityRepository;
  private final UserRepository userRepository;
  private final WorkingHoursRepository workingHoursRepository;

  public RecommendationServiceImpl(ActivityRepository activityRepository, UserRepository userRepository, WorkingHoursRepository workingHoursRepository){
    this.activityRepository = activityRepository;
    this.userRepository = userRepository;
    this.workingHoursRepository = workingHoursRepository;
  }


   @Override
    public List<RecommendationsDTO> findById(Long id, Double radiusKm, String context) {
      // TODO Auto-generated method stub
      

      throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }



  @Override
  public List<RecommendationsDTO> findByChoords(Double lat, Double lon, Double radiusKm, String context) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'findByChoords'");
  }




  
}


