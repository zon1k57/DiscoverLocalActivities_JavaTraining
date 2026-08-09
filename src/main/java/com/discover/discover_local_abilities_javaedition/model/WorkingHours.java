package com.discover.discover_local_abilities_javaedition.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class WorkingHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "activity_id")
    private Activity activityId;
    private String dayOfWeek;
    private LocalTime openTime;
    private LocalTime closedTime;
    private LocalTime breakTimeStart;
    private LocalTime breakTimeEnd;
    @Column(name = "is_24h")
    private boolean is24h;
    private boolean isClosed;
}
