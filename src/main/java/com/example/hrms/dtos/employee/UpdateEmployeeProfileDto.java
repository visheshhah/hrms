package com.example.hrms.dtos.employee;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateEmployeeProfileDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate joiningDate;
    private LocalDate dateOfBirth;
    private BigDecimal salary;
    private String designation;
    private Long departmentId;
    private Long managerId;
}
