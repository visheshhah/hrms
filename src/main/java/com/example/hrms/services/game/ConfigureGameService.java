package com.example.hrms.services.game;

import com.example.hrms.dtos.game.CreateGameConfigurationDto;
import com.example.hrms.dtos.game.GameConfigResponseDto;
import com.example.hrms.dtos.game.UpdateGameConfigurationDto;
import com.example.hrms.entities.ConfigureGame;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Game;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.ConfigureGameRepository;
import com.example.hrms.repositories.GameRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigureGameService {
    private final ConfigureGameRepository configureGameRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public Long createGameConfiguration(CreateGameConfigurationDto createGameConfigurationDto, Long userId) {
        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee createdByEmployee = creator.getEmployee();

        if(configureGameRepository.existsByGame_IdAndIsActiveFalse(createGameConfigurationDto.getGameId())){
            throw new IllegalStateException("Game already configured");
        }

        Game game = gameRepository.findById(createGameConfigurationDto.getGameId()).orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        ConfigureGame configureGame = new ConfigureGame();
        configureGame.setGame(game);
        configureGame.setCreatedBy(createdByEmployee);

        configureGame.setSlotDuration(createGameConfigurationDto.getSlotDuration());
        configureGame.setStartTime(createGameConfigurationDto.getStartTime());
        configureGame.setEndTime(createGameConfigurationDto.getEndTime());
        configureGame.setMaxPlayers(createGameConfigurationDto.getMaxPlayers());
        configureGame.setUpdatedBy(createdByEmployee);

        ConfigureGame savedConfigureGame = configureGameRepository.save(configureGame);
        return savedConfigureGame.getId();

    }

    public Long updateGameConfiguration(UpdateGameConfigurationDto updateGameConfigurationDto, Long userId, Long configureGameId) {
        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee updatedByEmployee = creator.getEmployee();

        ConfigureGame configureGame = configureGameRepository.findById(configureGameId).orElseThrow(() -> new ResourceNotFoundException(("Configuration not found")));
        configureGame.setUpdatedBy(updatedByEmployee);

        if (updateGameConfigurationDto.getSlotDuration() != null) {
            configureGame.setSlotDuration(updateGameConfigurationDto.getSlotDuration());
        }

        if(updateGameConfigurationDto.getStartTime() != null && updateGameConfigurationDto.getEndTime() != null) {
            if(updateGameConfigurationDto.getStartTime().isAfter(updateGameConfigurationDto.getEndTime())) {
                throw new InvalidParameterException("Start time cannot be after end time");
            }
            configureGame.setStartTime(updateGameConfigurationDto.getStartTime());
            configureGame.setEndTime(updateGameConfigurationDto.getEndTime());
        }

        if(updateGameConfigurationDto.getStartTime() != null) {
            if(updateGameConfigurationDto.getStartTime().isAfter(configureGame.getEndTime())) {
                throw new InvalidParameterException("Start time cannot be after end time");
            }
            configureGame.setStartTime(updateGameConfigurationDto.getStartTime());
        }

        if(updateGameConfigurationDto.getEndTime() != null) {
            if(updateGameConfigurationDto.getEndTime().isBefore(configureGame.getStartTime())) {
                throw new InvalidParameterException("End time cannot be before start time");
            }
            configureGame.setEndTime(updateGameConfigurationDto.getEndTime());
        }


        if(updateGameConfigurationDto.getMaxPlayers() != null) {
            configureGame.setMaxPlayers(updateGameConfigurationDto.getMaxPlayers());
        }

        ConfigureGame savedConfigureGame = configureGameRepository.save(configureGame);
        return savedConfigureGame.getId();

    }

    public List<GameConfigResponseDto> getGameConfigurations(Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }


        List<ConfigureGame> configureGames = configureGameRepository.findAll();

        return configureGames.stream().
                map(configureGame -> {
                    GameConfigResponseDto gameConfigResponseDto = new GameConfigResponseDto();
                    gameConfigResponseDto.setId(configureGame.getId());
                    gameConfigResponseDto.setStartTime(configureGame.getStartTime());
                    gameConfigResponseDto.setEndTime(configureGame.getEndTime());
                    gameConfigResponseDto.setMaxPlayers(configureGame.getMaxPlayers());
                    gameConfigResponseDto.setSlotDuration(configureGame.getSlotDuration());
                    gameConfigResponseDto.setGameName(configureGame.getGame().getGameName());
                    return gameConfigResponseDto;
                })
                .toList();
    }

    public GameConfigResponseDto getGameConfigurationById(Long configureGameId, Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }


        ConfigureGame configureGame = configureGameRepository.findById(configureGameId).orElseThrow(() -> new ResourceNotFoundException("Game configuration does not exist"));
        GameConfigResponseDto gameConfigResponseDto = new GameConfigResponseDto();
        gameConfigResponseDto.setId(configureGame.getId());
        gameConfigResponseDto.setStartTime(configureGame.getStartTime());
        gameConfigResponseDto.setEndTime(configureGame.getEndTime());
        gameConfigResponseDto.setMaxPlayers(configureGame.getMaxPlayers());
        gameConfigResponseDto.setSlotDuration(configureGame.getSlotDuration());
        gameConfigResponseDto.setGameName(configureGame.getGame().getGameName());

        return gameConfigResponseDto;
    }

    public void deleteGameConfiguration(Long configureGameId, Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }

        ConfigureGame configureGame = configureGameRepository.findById(configureGameId).orElseThrow(() -> new ResourceNotFoundException("Game configuration does not exist"));
        configureGame.setDeletedBy(user.getEmployee());
        configureGame.setDeletedAt(Instant.now());
        configureGame.setIsActive(false);
        configureGameRepository.save(configureGame);



    }
}
