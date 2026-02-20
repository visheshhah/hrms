package com.example.hrms.dtos.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class UpdateGameConfigurationDto {
    private LocalTime startTime;

    private LocalTime endTime;

    //temporary
    @Min(value = 1)
    private Integer minPlayers;

    @Max(value = 4)
    private Integer maxPlayers;

    private Integer slotDuration;
}
