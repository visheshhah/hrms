package com.example.hrms.controllers.game;

import com.example.hrms.dtos.game.GameSlotResponseDto;
import com.example.hrms.services.game.GameSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slot")
public class GameSlotController {
    private final GameSlotService gameSlotService;

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<GameSlotResponseDto>>  getGameSlot(@PathVariable("gameId") Long gameId) {
        return ResponseEntity.ok(gameSlotService.getGameSlotsByGameId(gameId));
    }

    @GetMapping("/{gameSlotId}")
    public ResponseEntity<GameSlotResponseDto>  getGameSlotById(@PathVariable("gameSlotId") Long gameSlotId) {
        return ResponseEntity.ok(gameSlotService.getGameSlotById(gameSlotId));
    }
}
