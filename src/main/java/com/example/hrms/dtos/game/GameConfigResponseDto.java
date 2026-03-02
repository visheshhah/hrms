package com.example.hrms.dtos.game;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class GameConfigResponseDto {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxPlayers;
    private Integer slotDuration;
    private String gameName;
}
