package com.example.hrms.dtos.categoryType;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategoryCreateDto {

    @NotBlank(message = "Name is required")
    private String name;
}