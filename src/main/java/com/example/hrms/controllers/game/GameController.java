package com.example.hrms.controllers.game;

import com.example.hrms.dtos.game.CreateGameConfigurationDto;
import com.example.hrms.dtos.game.GameConfigResponseDto;
import com.example.hrms.dtos.game.GameResponseDto;
import com.example.hrms.dtos.game.UpdateGameConfigurationDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.game.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;

    @GetMapping
    public ResponseEntity<List<GameResponseDto>> getAllGames()
    {
        List<GameResponseDto> gameResponseDtos = gameService.getAllGames();
        return ResponseEntity.ok(gameResponseDtos);
    }
}
