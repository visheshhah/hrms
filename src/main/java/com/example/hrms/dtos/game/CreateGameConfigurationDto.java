package com.example.hrms.dtos.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Getter
@Service
public class CreateGameConfigurationDto {
    @NotEmpty
    private LocalTime startTime;

    @NotEmpty
    private LocalTime endTime;

    //temporary
    @Min(value = 1)
    private Integer minPlayers;

    @Max(value = 4)
    private Integer maxPlayers;

    @NotEmpty
    private Integer slotDuration;

    @NotEmpty
    private Long gameId;

}
