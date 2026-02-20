package com.example.hrms.dtos.travel;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class EmployeeTravelDto{

    @NotEmpty
    private Long employeeId;

}
