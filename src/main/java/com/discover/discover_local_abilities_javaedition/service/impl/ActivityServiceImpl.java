package com.discover.discover_local_abilities_javaedition.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.discover.discover_local_abilities_javaedition.dto.ActivityWithHoursDTO;
import com.discover.discover_local_abilities_javaedition.dto.WorkingHoursDTO;
import com.discover.discover_local_abilities_javaedition.model.Activity;
import com.discover.discover_local_abilities_javaedition.model.WorkingHours;
import com.discover.discover_local_abilities_javaedition.model.exceptions.ActivityNotFoundException;
import com.discover.discover_local_abilities_javaedition.repository.ActivityRepository;
import com.discover.discover_local_abilities_javaedition.repository.WorkingHoursRepository;
import com.discover.discover_local_abilities_javaedition.service.ActivityService;

@Service
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final WorkingHoursRepository workingHoursRepository;

    public ActivityServiceImpl(ActivityRepository activityRepository, WorkingHoursRepository workingHoursRepository){
        this.activityRepository = activityRepository;
        this.workingHoursRepository = workingHoursRepository;
    }


    // Function for all activity fetch
    @Override
    public List<ActivityWithHoursDTO> findAll(String category, Double minRating, Integer minRatingCount) {
        List<Activity> activities = activityRepository.findAll();

        if (category != null && !category.isBlank()) {
        String cat = category.toLowerCase();
        activities = activities.stream()
                .filter(a -> a.getType() != null && a.getType().toLowerCase().contains(cat))
                .toList();
         }
        if (minRating != null) {
                activities = activities.stream()
                .filter(a -> a.getRating() != null && a.getRating() >= minRating)
                .toList();
        }
        if (minRatingCount != null) {
                activities = activities.stream()
                .filter(a -> a.getUserRatingCount() != null && a.getUserRatingCount() >= minRatingCount)
                .toList();
        }
        
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

    // Function for rounding and finding nearby activities
    @Override
    public List<ActivityWithHoursDTO> findNearby(Double lat, Double lon, Double radiusKm,
                                              String category) {
    if (radiusKm == null){
      radiusKm = 1.0;
    }
    double latDelta = radiusKm / 111.0;
    double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

    List<Activity> activities = activityRepository.findWithinBoundingBox(
        lat - latDelta, lat + latDelta,
        lon - lonDelta, lon + lonDelta,
        category
    );

    List<Long> activityIds = activities.stream().map(Activity::getId).toList();
    List<WorkingHours> workingHours = workingHoursRepository.findByActivityId_IdIn(activityIds);
    Map<Long, List<WorkingHoursDTO>> hoursByActivityId = workingHours.stream()
        .collect(Collectors.groupingBy(wh -> wh.getActivityId().getId(),
            Collectors.mapping(this::toWorkingHoursDTO, Collectors.toList())));

    return activities.stream()
        .map(activity -> new ActivityWithHoursDTO(
            activity.getId(), activity.getName(), activity.getPhoneNumber(),
            activity.getLatitude(), activity.getLongitude(),
            activity.getRating(), activity.getUserRatingCount(), activity.getType(),
            hoursByActivityId.getOrDefault(activity.getId(), List.of())
        ))
        .toList();
}

    @Override
    public List<WorkingHours> listWorkingHoursOfActivity(Long id) {
        return List.of();
    }
}
