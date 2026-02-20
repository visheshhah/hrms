package com.example.hrms.dtos.travel;

import com.example.hrms.enums.TravelStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateTravelPlanDto {

    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private TravelStatus status;

    private String sourceLocation;

    private String destinationLocation;

    private Boolean isInternational;

    private List<EmployeeTravelDto> employees;
}
