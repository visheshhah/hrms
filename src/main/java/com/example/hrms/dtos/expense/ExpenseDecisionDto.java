package com.example.hrms.dtos.expense;

import com.example.hrms.enums.ExpenseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseDecisionDto {
    private String remark;
    private ExpenseStatus status;
}

