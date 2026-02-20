package com.example.hrms.dtos.expense;

import com.example.hrms.enums.ExpenseStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HrDecisionDto {
    @NotBlank
    private Long expenseId;

    private String remark;
}
