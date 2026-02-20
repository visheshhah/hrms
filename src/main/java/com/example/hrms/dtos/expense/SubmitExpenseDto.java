package com.example.hrms.dtos.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitExpenseDto {
    @NotBlank
    private String description;

    @NotEmpty
    private Long categoryId;

    @NotEmpty
    private BigDecimal amount;


}
