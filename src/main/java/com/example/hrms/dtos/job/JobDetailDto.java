package com.example.hrms.dtos.job;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class JobDetailDto {
    private Long id;
    private String title;

    private String description;

    private String companyName;

    private String location;

    private double minExperience;
    private double maxExperience;

    private String jobType;

    private String workPlaceType;

    private Set<Long> reviewerIds;
}
