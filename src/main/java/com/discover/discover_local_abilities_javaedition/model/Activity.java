package com.discover.discover_local_abilities_javaedition.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private int userRatingCount;
    private String type;
}
