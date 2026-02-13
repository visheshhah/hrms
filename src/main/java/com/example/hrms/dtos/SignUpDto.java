package com.example.hrms.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class SignUpDto{


        @NotBlank(message = "Please enter username")
        String username;

        @Email(message = "Invalid email format")
        String email;

        @NotBlank(message = "Please enter password")
        String password;

        @NotBlank
        private Long employeeId;

        @NotBlank
        private Set<String> roles;
}

