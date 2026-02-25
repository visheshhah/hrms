package com.example.hrms.dtos.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class AddEmployeeDto {
    @NotEmpty
    private LocalDate dateOfBirth;

    @NotBlank
    private String designation;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private LocalDate joiningDate;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private BigDecimal salary;

    @NotBlank
    private Long departmentId;

    private Long managerId;
}
