package com.example.hrms.dtos;

import com.example.hrms.enums.ERole;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CurrentUserResponseDto {

    private Long userId;

    private Long employeeId;

    private String firstName;
    private String lastName;
    private String email;

    private Set<ERole> roles;
}
