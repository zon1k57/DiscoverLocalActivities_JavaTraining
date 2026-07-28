package com.discover.discover_local_abilities_javaedition.service.impl;

import com.discover.discover_local_abilities_javaedition.dto.WorkingHoursDTO;
import com.discover.discover_local_abilities_javaedition.model.Activity;
import com.discover.discover_local_abilities_javaedition.model.WorkingHours;
import com.discover.discover_local_abilities_javaedition.model.exceptions.ActivityNotFoundException;
import com.discover.discover_local_abilities_javaedition.repository.ActivityRepository;
import com.discover.discover_local_abilities_javaedition.repository.WorkingHoursRepository;
import com.discover.discover_local_abilities_javaedition.service.ActivityService;
import com.discover.discover_local_abilities_javaedition.dto.ActivityWithHoursDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final WorkingHoursRepository workingHoursRepository;

    public ActivityServiceImpl(ActivityRepository activityRepository, WorkingHoursRepository workingHoursRepository){
        this.activityRepository = activityRepository;
        this.workingHoursRepository = workingHoursRepository;
    }


    @Override
    public List<ActivityWithHoursDTO> findAll() {
        List<Activity> activities = activityRepository.findAll();
        List<Long> activityIds = activities.stream().map(Activity::getId).toList();

        List<WorkingHours> workingHours = workingHoursRepository.findByActivityId_IdIn(activityIds);

        Map<Long, List<WorkingHoursDTO>> hoursByActivityId = workingHours.stream()
                .collect(Collectors.groupingBy(
                        wh -> wh.getActivityId().getId(),
                        Collectors.mapping(this::toWorkingHoursDTO, Collectors.toList())
                ));

        return activities.stream()
                .map(activity -> new ActivityWithHoursDTO(
                        activity.getId(),
                        activity.getName(),
                        activity.getPhoneNumber(),
                        activity.getLatitude(),
                        activity.getLongitude(),
                        activity.getRating(),
                        activity.getUserRatingCount(),
                        activity.getType(),
                        hoursByActivityId.getOrDefault(activity.getId(), List.of())
                ))
                .toList();
    }

    private WorkingHoursDTO toWorkingHoursDTO(WorkingHours wh) {
        return new WorkingHoursDTO(
                wh.getDayOfWeek(),
                wh.getOpenTime(),
                wh.getClosedTime(),
                wh.getBreakTimeStart(),
                wh.getBreakTimeEnd(),
                wh.is24h(),
                wh.isClosed()
        );
    }

    @Override
    public ActivityWithHoursDTO findByIndex(Long id) {
        Activity activity = activityRepository.findById(id).orElseThrow(() -> new ActivityNotFoundException("Activity not found with id ",id));

        List<WorkingHoursDTO> hours = workingHoursRepository.findByActivityId_Id(id).stream()
                .map(this::toWorkingHoursDTO)
                .toList();

        return new ActivityWithHoursDTO(
                activity.getId(),
                activity.getName(),
                activity.getPhoneNumber(),
                activity.getLatitude(),
                activity.getLongitude(),
                activity.getRating(),
                activity.getUserRatingCount(),
                activity.getType(),
                hours
        );
    }

    @Override
    public List<WorkingHours> listWorkingHoursOfActivity(Long id) {
        return List.of();
    }
}
