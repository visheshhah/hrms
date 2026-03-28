package com.example.hrms.dtos.role;

import com.example.hrms.enums.ERole;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class EmployeeRoleResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String designation;
    private String department;
    private Set<ERole> roles;
}
