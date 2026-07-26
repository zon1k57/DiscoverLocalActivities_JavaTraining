package com.discover.discover_local_abilities_javaedition.service;

import com.discover.discover_local_abilities_javaedition.model.Activity;
import com.discover.discover_local_abilities_javaedition.model.WorkingHours;

import java.util.List;

public interface ActivityService {
    List<Activity> findAll();
    Activity findByIndex(Long id);
    List<WorkingHours> listWorkingHoursOfActivity(Long id);
}
