package com.example.hrms.dtos.expense;

import com.example.hrms.entities.CategoryType;
import com.example.hrms.entities.Expense;
import com.example.hrms.enums.ExpenseStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EmployeeExpenseResponseDto {
    private Long id;
    private Long travelPlanId;
    private String remark;
    private String categoryName;
    private ExpenseStatus expenseStatus;
    private BigDecimal amount;
    private String description;
    //new
    private String filePath;
}
