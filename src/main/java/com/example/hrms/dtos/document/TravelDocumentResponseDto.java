package com.example.hrms.dtos.document;

import com.example.hrms.enums.EOwnerType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TravelDocumentResponseDto {
    private Long travelDocumentId;
    private Long travelPlanId;
    private Long employeeId;
    private String documentTypeName;
    private String fileName;
    private String uploadedByName;
    private EOwnerType uploadedByRole;
    //private Instant uploadedAt;
}
