package com.example.hrms.dtos.travel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String designation;
    private String department;
    private Long managerId;
    private Boolean isOnTravel;
    private Boolean isLimitReached;
    private Boolean isAlreadyRegistered;
}
