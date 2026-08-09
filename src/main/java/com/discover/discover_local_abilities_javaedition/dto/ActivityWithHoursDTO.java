package com.discover.discover_local_abilities_javaedition.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivityWithHoursDTO {
    private Long id;
    private String name;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer userRatingCount;
    private String type;
    private List<WorkingHoursDTO> workingHours;
}