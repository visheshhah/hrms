package com.example.hrms.services.game;

import com.example.hrms.dtos.game.*;
import com.example.hrms.entities.ConfigureGame;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Game;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.ConfigureGameRepository;
import com.example.hrms.repositories.GameRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ConfigureGameRepository configureGameRepository;


    public List<GameResponseDto> findAll() {
        List<Game> games = gameRepository.findByIsActiveTrue();
        return games.stream()
                .map(g -> modelMapper.map(g, GameResponseDto.class))
                .toList();
    }

    public GameResponseDto create(GameRequestDto dto) {

        Optional<Game> existing = gameRepository.findByGameName(dto.getGameName());

        if (existing.isPresent()) {
            Game game = existing.get();

            if (!game.getIsActive()) {
                game.setIsActive(true);
                return modelMapper.map(gameRepository.save(game), GameResponseDto.class);
            }

            throw new RuntimeException("Game already exists");
        }

        Game game = new Game();
        game.setGameName(dto.getGameName());

        return modelMapper.map(gameRepository.save(game), GameResponseDto.class);
    }

    public GameResponseDto findById(Long id) {
        Game game = gameRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));

        return modelMapper.map(game, GameResponseDto.class);
    }

    public GameResponseDto update(Long id, GameRequestDto dto) {

        Game game = gameRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));

        Optional<Game> existing = gameRepository.findByGameName(dto.getGameName());

        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new RuntimeException("Game name already in use");
        }

        game.setGameName(dto.getGameName());

        return modelMapper.map(gameRepository.save(game), GameResponseDto.class);
    }

    public void delete(Long id) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found with id: " + id));

        if (!game.getIsActive()) {
            throw new RuntimeException("Game already deleted");
        }

        game.setIsActive(false);
        gameRepository.save(game);
    }

//    public Long createGameConfiguration(CreateGameConfigurationDto createGameConfigurationDto, Long userId) {
//        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        Employee createdByEmployee = creator.getEmployee();
//
//        Game game = gameRepository.findById(createGameConfigurationDto.getGameId()).orElseThrow(() -> new ResourceNotFoundException("Game not found"));
//
//        ConfigureGame configureGame = new ConfigureGame();
//        configureGame.setGame(game);
//        configureGame.setCreatedBy(createdByEmployee);
//
//        configureGame.setSlotDuration(createGameConfigurationDto.getSlotDuration());
//        configureGame.setStartTime(createGameConfigurationDto.getStartTime());
//        configureGame.setEndTime(createGameConfigurationDto.getEndTime());
//        configureGame.setMaxPlayers(createGameConfigurationDto.getMaxPlayers());
//        configureGame.setUpdatedBy(createdByEmployee);
//
//        ConfigureGame savedConfigureGame = configureGameRepository.save(configureGame);
//        return savedConfigureGame.getId();
//
//    }
//
//    public Long updateGameConfiguration(UpdateGameConfigurationDto  updateGameConfigurationDto, Long userId, Long configureGameId) {
//        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        Employee updatedByEmployee = creator.getEmployee();
//
//        ConfigureGame configureGame = configureGameRepository.findById(configureGameId).orElseThrow(() -> new ResourceNotFoundException(("Configuration not found")));
//        configureGame.setUpdatedBy(updatedByEmployee);
//
//        if (updateGameConfigurationDto.getSlotDuration() != null) {
//            configureGame.setSlotDuration(updateGameConfigurationDto.getSlotDuration());
//        }
//
//        if(updateGameConfigurationDto.getStartTime() != null && updateGameConfigurationDto.getEndTime() != null) {
//                if(updateGameConfigurationDto.getStartTime().isAfter(updateGameConfigurationDto.getEndTime())) {
//                    throw new InvalidParameterException("Start time cannot be after end time");
//                }
//                configureGame.setStartTime(updateGameConfigurationDto.getStartTime());
//                configureGame.setEndTime(updateGameConfigurationDto.getEndTime());
//        }
//
//        if(updateGameConfigurationDto.getStartTime() != null) {
//                if(updateGameConfigurationDto.getStartTime().isAfter(configureGame.getEndTime())) {
//                    throw new InvalidParameterException("Start time cannot be after end time");
//                }
//                configureGame.setStartTime(updateGameConfigurationDto.getStartTime());
//        }
//
//        if(updateGameConfigurationDto.getEndTime() != null) {
//                if(updateGameConfigurationDto.getEndTime().isBefore(configureGame.getStartTime())) {
//                    throw new InvalidParameterException("End time cannot be before start time");
//                }
//                configureGame.setEndTime(updateGameConfigurationDto.getEndTime());
//        }
//
//
//        if(updateGameConfigurationDto.getMaxPlayers() != null) {
//            configureGame.setMaxPlayers(updateGameConfigurationDto.getMaxPlayers());
//        }
//
//        ConfigureGame savedConfigureGame = configureGameRepository.save(configureGame);
//        return savedConfigureGame.getId();
//
//    }
//
//    private List<GameConfigResponseDto> getGameConfigurations(Long userId) {
//        List<ConfigureGame> configureGames = configureGameRepository.findAll();
//
//        return configureGames.stream().
//                map(configureGame -> {
//                    GameConfigResponseDto gameConfigResponseDto = new GameConfigResponseDto();
//                    gameConfigResponseDto.setId(configureGame.getId());
//                    gameConfigResponseDto.setStartTime(configureGame.getStartTime());
//                    gameConfigResponseDto.setEndTime(configureGame.getEndTime());
//                    gameConfigResponseDto.setMaxPlayers(configureGame.getMaxPlayers());
//                    gameConfigResponseDto.setSlotDuration(configureGame.getSlotDuration());
//                    gameConfigResponseDto.setGameName(configureGame.getGame().getGameName());
//                    return gameConfigResponseDto;
//                })
//                .toList();
//    }
}
