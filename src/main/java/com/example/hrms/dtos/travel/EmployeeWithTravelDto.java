package com.example.hrms.dtos.travel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class EmployeeWithTravelDto {
    private Long id;
    private String name;
    private String designation;
    private Boolean isOnTravel;
}
