package com.example.hrms.controllers.game;

import com.example.hrms.dtos.game.CreateGameConfigurationDto;
import com.example.hrms.dtos.game.GameConfigResponseDto;
import com.example.hrms.dtos.game.UpdateGameConfigurationDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.game.ConfigureGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/configure")
public class ConfigureGameController {
    private final ConfigureGameService configureGameService;

    @PostMapping("/create")
    public ResponseEntity<Long> createGameConfiguration(@RequestBody CreateGameConfigurationDto createGameConfigurationDto, @AuthenticationPrincipal MyUserDetails userDetails)
    {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(configureGameService.createGameConfiguration(createGameConfigurationDto, userId));
    }

    @PatchMapping("/update/{configure-game-id}")
    public ResponseEntity<Long> updateGameConfiguration(@PathVariable("configure-game-id") Long configureGameId, @RequestBody UpdateGameConfigurationDto updateGameConfigurationDto, @AuthenticationPrincipal MyUserDetails userDetails){
        Long userId = userDetails.getId();
        return ResponseEntity.ok(configureGameService.updateGameConfiguration(updateGameConfigurationDto, userId, configureGameId));
    }

    @GetMapping
    public ResponseEntity<List<GameConfigResponseDto>> getGameConfiguration(@AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {
        return ResponseEntity.ok(configureGameService.getGameConfigurations(userDetails.getId()));
    }

    @GetMapping("/{configure-game-id}")
    public ResponseEntity<GameConfigResponseDto> getGameConfigurationById(@PathVariable("configure-game-id") Long configureGameId, @AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {
        return ResponseEntity.ok(configureGameService.getGameConfigurationById(configureGameId, userDetails.getId()));
    }

    @DeleteMapping("/{configure-game-id}")
    public ResponseEntity<Void> deleteGameConfiguration(@PathVariable("configure-game-id") Long configureGameId, @AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {
        configureGameService.deleteGameConfiguration(configureGameId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
