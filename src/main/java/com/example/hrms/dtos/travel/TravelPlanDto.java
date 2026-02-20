package com.example.hrms.dtos.travel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TravelPlanDto{
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank
    private String sourceLocation;

    @NotBlank
    private String destinationLocation;

    @NotNull
    private Boolean isInternational;

    @NotEmpty
    private List<EmployeeTravelDto> employees;
}
