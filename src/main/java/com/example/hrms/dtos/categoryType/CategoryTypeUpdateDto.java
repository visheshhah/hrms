package com.example.hrms.dtos.categoryType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryTypeUpdateDto {

    @NotBlank(message = "Name is required")
    private String name;
}