package com.example.hrms.dtos.job;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShareJobDto {
    @NotBlank
    private String email;
}
