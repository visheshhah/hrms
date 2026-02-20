package com.example.hrms.dtos.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateJobDto {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String companyName;

    @NotBlank
    private String location;

    @NotEmpty
    private double minExperience;
    @NotEmpty
    private double maxExperience;

    @NotBlank
    private String jobType;

    @NotBlank
    private String workPlaceType;

}
