package com.example.hrms.exceptions;

import com.example.hrms.dtos.travel.EmployeeConflictDto;

import java.util.List;

public class EmployeeTravelConflictException extends RuntimeException {

    private final List<EmployeeConflictDto> conflicts;

    public EmployeeTravelConflictException(List<EmployeeConflictDto> conflicts) {
        super("Some employees are already booked for the selected dates");
        this.conflicts = conflicts;
    }

    public List<EmployeeConflictDto> getConflicts() {
        return conflicts;
    }
}
