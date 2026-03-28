package com.example.hrms.dtos.travel;

import com.example.hrms.enums.TravelStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TravelPlanDetailDto {
    private Long travelPlanId;

    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private TravelStatus status;

    private String sourceLocation;

    private String destinationLocation;

    private Boolean isInternational;

    private Instant createdAt;

    private Long createdById;

    List<EmployeeDto> participants;
}
