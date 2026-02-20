package com.example.hrms.dtos.document;

import lombok.Getter;

import java.time.Instant;

@Getter
public class TravelDocumentResponseDto {
    private Long id;
    private Long travelPlanId;
    private Long employeeId;
    private String documentTypeName;
    private String fileName;
    private String uploadedByName;
    private String uploadedByRole;
    private Instant uploadedAt;
}
