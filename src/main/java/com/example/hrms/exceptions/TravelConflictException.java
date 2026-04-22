package com.example.hrms.exceptions;

import com.example.hrms.entities.Employee;

import java.util.List;

public class TravelConflictException extends RuntimeException {

    private final List<Employee> employees;

    public TravelConflictException(List<Employee> employees) {
        super("Some employees are on travel");
        this.employees = employees;
    }

    public List<Employee> getEmployees() {
        return employees;
    }
}