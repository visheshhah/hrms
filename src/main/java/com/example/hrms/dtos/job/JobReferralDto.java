package com.example.hrms.dtos.job;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class JobReferralDto {
    private String comment;

    @NotBlank
    private String friendName;

    private String friendEmail;

}
