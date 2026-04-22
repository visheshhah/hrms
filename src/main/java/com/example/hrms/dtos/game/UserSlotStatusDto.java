package com.example.hrms.dtos.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSlotStatusDto {
    private Boolean isOnTravel;
    private Boolean isLimitReached;
    private Boolean isRegistered;
}
