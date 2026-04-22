package com.example.hrms.controllers.game;

import com.example.hrms.dtos.game.*;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.game.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {
    private final GameService gameService;

    @PostMapping
    public ResponseEntity<GameResponseDto> create(@Valid @RequestBody GameRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gameService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<GameResponseDto>> getAll() {
        return ResponseEntity.ok(gameService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody GameRequestDto dto) {

        return ResponseEntity.ok(gameService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gameService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
