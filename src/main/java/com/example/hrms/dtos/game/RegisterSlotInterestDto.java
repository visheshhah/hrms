package com.example.hrms.dtos.game;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegisterSlotInterestDto {
    List<Long> employeeIds;
}
