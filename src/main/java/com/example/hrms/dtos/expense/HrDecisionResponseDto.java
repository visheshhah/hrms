package com.example.hrms.dtos.expense;

import com.example.hrms.enums.ExpenseStatus;
import lombok.Data;

@Data
public class HrDecisionResponseDto {
    private Long id;
    private ExpenseStatus status;
}
