package com.example.hrms.dtos.documentType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class DocumentTypeCreateDto {

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private Set<String> allowedFormats;
}