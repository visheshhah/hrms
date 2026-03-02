package com.example.hrms.dtos.game;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateEmployeeGameInterestDto {
    private Set<Long> gameIds;
}
