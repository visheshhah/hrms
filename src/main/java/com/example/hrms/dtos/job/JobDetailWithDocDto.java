package com.example.hrms.dtos.job;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobDetailWithDocDto {
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

    private Long jdId;

    private String fileName;
}
