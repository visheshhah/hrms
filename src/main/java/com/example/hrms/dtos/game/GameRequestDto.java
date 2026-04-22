package com.example.hrms.dtos.game;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRequestDto {
    @NotBlank(message = "Game name is required")
    private String gameName;
}
