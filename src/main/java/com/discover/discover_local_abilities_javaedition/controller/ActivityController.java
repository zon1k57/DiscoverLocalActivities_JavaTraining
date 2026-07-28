package com.discover.discover_local_abilities_javaedition.controller;

import com.discover.discover_local_abilities_javaedition.dto.ActivityWithHoursDTO;
import com.discover.discover_local_abilities_javaedition.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value="/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService){
        this.activityService = activityService;
    }

    @GetMapping
    public List<ActivityWithHoursDTO> listAll(){
        return activityService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityWithHoursDTO> findByIndex(@PathVariable Long id){ return ResponseEntity.ok(activityService.findByIndex(id));}
}

