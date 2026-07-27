package com.discover.discover_local_abilities_javaedition.dto;

import com.discover.discover_local_abilities_javaedition.model.WorkingHours;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class ActivityWithHoursDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private int userRatingCount;
    private String type;
    private List<WorkingHoursDTO> workingHours;
}