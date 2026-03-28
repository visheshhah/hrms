package com.example.hrms.dtos.employee;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeDetailResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String designation;
    private String phoneNumber;
    private LocalDate joiningDate;
    private LocalDate dateOfBirth;
    private String departmentName;
    private String managerName;
}
