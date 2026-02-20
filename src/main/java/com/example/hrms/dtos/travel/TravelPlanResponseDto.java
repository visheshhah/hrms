package com.example.hrms.dtos.travel;
import com.example.hrms.enums.TravelStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelPlanResponseDto{

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



}
