package com.example.hrms.dtos.job;

import lombok.Data;

import java.time.Instant;

@Data
public class JobResponseDto {

    private Long id;
    private String title;

    private String description;

    private String companyName;

    private String location;

    private double minExperience;
    private double maxExperience;

    private String jobType;

    private String workPlaceType;

    private String status;

}
