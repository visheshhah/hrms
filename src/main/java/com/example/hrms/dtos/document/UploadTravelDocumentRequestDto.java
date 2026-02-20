package com.example.hrms.dtos.document;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadTravelDocumentRequestDto {
    @NotNull
    private Long documentTypeId;

    private Long employeeId;
}
