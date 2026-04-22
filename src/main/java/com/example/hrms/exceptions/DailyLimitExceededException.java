package com.example.hrms.exceptions;

import com.example.hrms.dtos.game.EmployeeLimitDto;
import com.example.hrms.entities.Employee;

import java.util.List;

public class DailyLimitExceededException extends RuntimeException {

    private final List<EmployeeLimitDto> employees;

    public DailyLimitExceededException(List<Employee> employees) {
        super("Daily booking limit exceeded");
        this.employees = employees.stream()
                .map(EmployeeLimitDto::new)
                .toList();
    }

    public List<EmployeeLimitDto> getEmployees() {
        return employees;
    }
}