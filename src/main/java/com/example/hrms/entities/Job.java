package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;
    private String companyName;
    private double minExperience;
    private double maxExperience;
    private String jobType;
    private String workPlaceType;
    private String status = "Open";


    @ManyToOne
    @JoinColumn(name = "creator_id")
    private Employee creator;


}
