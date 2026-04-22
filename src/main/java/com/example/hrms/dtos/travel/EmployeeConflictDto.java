package com.example.hrms.dtos.travel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EmployeeConflictDto {
    private Long employeeId;
    private String employeeName;
    private LocalDate bookedFrom;
    private LocalDate bookedTo;
}
