package com.discover.discover_local_abilities_javaedition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class WorkingHoursDTO {
    private String dayOfWeek;
    private LocalTime openTime;
    private LocalTime closedTime;
    private LocalTime breakTimeStart;
    private LocalTime breakTimeEnd;
    private boolean is24h;
    private boolean isClosed;
}