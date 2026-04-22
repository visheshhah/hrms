package com.example.hrms.dtos.game;

import com.example.hrms.entities.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeLimitDto {
    private Long id;
    private String name;

    public EmployeeLimitDto(Employee e) {
        this.id = e.getId();
        this.name = e.getFirstName() + " " + e.getLastName();
    }
}