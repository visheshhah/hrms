package com.example.hrms.dtos.job;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ViewJobReferralDto {
    private long id;
    private String employeeName;
    private String comment;
    private String friendName;
    private String friendEmail;
    private String fileName;
}
