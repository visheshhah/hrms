package com.example.hrms.dtos.job;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class UpdateJobDto {
    private String title;
    private String description;
    private String location;
    private String companyName;
    private Double minExperience;
    private Double maxExperience;
    private String jobType;
    private String workPlaceType;
    Set<Long> jobCvReviewerIds;

}
