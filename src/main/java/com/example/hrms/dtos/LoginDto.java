package com.example.hrms.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto{
        @NotBlank(message = "Invalid email format")
        private String username;

        @NotBlank(message = "Please enter password")
        private String password;
}