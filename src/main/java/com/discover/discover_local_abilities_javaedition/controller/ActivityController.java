package com.discover.discover_local_abilities_javaedition.controller;

import com.discover.discover_local_abilities_javaedition.model.Activity;
import com.discover.discover_local_abilities_javaedition.service.ActivityService;
import com.discover.discover_local_abilities_javaedition.service.impl.ActivityServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value="/api/activities")
public class ActivityController {

    private final ActivityServiceImpl activityService;

    public ActivityController(ActivityServiceImpl activityService){
        this.activityService = activityService;
    }

    @GetMapping
    public List<Activity> listAll(){
        return activityService.findAll();
    }
}
