package com.discover.discover_local_abilities_javaedition.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
public class WorkingHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Activity activityId;
    private String dayOfWeek;
    private LocalTime openTime;
    private LocalTime closedTime;
    private LocalTime breakTimeStart;
    private LocalTime breakTimeEnd;
    private boolean is24h;
    private boolean isClosed;
}
