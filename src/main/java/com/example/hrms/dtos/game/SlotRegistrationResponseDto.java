package com.example.hrms.dtos.game;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class SlotRegistrationResponseDto {
    private Long slotRegistrationId;
    private Long slotId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxPlayers;
    private String gameName;
}
