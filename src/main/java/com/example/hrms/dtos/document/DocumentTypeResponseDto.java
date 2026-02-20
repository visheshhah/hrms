package com.example.hrms.dtos.document;

import lombok.Data;

@Data
public class DocumentTypeResponseDto {
    private Long id;
    private String name;
    private Boolean isMandatory;
    private String allowedFormats;
}
