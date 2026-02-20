package com.example.hrms.dtos.job;

import lombok.Getter;

@Getter
public class JobReferralResponseDto {
    private Long id;

    private String comment;

    private String friendName;

    private String friendEmail;
}
