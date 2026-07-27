package com.discover.discover_local_abilities_javaedition.service.impl;

import com.discover.discover_local_abilities_javaedition.model.Activity;
import com.discover.discover_local_abilities_javaedition.model.WorkingHours;
import com.discover.discover_local_abilities_javaedition.repository.ActivityRepository;
import com.discover.discover_local_abilities_javaedition.repository.WorkingHoursRepository;
import com.discover.discover_local_abilities_javaedition.service.ActivityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final WorkingHoursRepository workingHoursRepository;

    public ActivityServiceImpl(ActivityRepository activityRepository, WorkingHoursRepository workingHoursRepository){
        this.activityRepository = activityRepository;
        this.workingHoursRepository = workingHoursRepository;
    }

    @Override
    public List<Activity> findAll() {
        return activityRepository.findAll();
    }

    @Override
    public Activity findByIndex(Long id) {
        return null;
    }

    @Override
    public List<WorkingHours> listWorkingHoursOfActivity(Long id) {
        return List.of();
    }
}
