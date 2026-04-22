package com.example.hrms.dtos.documentType;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class DocumentTypeResponseDto {

    private Long id;
    private String code;
    private String name;
    private Boolean isActive;
    private Set<String> allowedFormats;
}