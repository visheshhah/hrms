package com.example.hrms.dtos.expense;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitExpenseDto {
    @NotBlank
    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;


}
