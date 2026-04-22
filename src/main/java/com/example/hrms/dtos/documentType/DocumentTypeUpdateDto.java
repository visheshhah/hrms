package com.example.hrms.dtos.documentType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class DocumentTypeUpdateDto {

    @NotBlank(message = "Name is required")
    private String name;

    private Set<String> allowedFormats;
}