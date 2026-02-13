package com.example.hrms.dtos.travel;

import lombok.Data;

@Data
public class EmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String designation;
    private String department;
}
