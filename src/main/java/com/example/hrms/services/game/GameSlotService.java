package com.example.hrms.services.game;

import com.example.hrms.dtos.game.GameSlotResponseDto;
import com.example.hrms.entities.ConfigureGame;
import com.example.hrms.entities.Game;
import com.example.hrms.entities.GameSlot;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.ConfigureGameRepository;
import com.example.hrms.repositories.GameRepository;
import com.example.hrms.repositories.GameSlotRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameSlotService {
    private final GameSlotRepository gameSlotRepository;
    private final ConfigureGameRepository configureGameRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public List<GameSlotResponseDto> getGameSlotsByGameId(Long gameId) {
        gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game does not exist"));

        LocalTime now = LocalTime.now();
        LocalTime oneHourLater = now.plusHours(1);

        List<GameSlot> gameSlots = new ArrayList<>();

        for (GameSlot gameSlot : gameSlotRepository.findByGameIdAndSlotDate(gameId, LocalDate.now())){
            if(gameSlot.getStartTime().isAfter(now) && gameSlot.getStartTime().isAfter(oneHourLater)) {
                gameSlots.add(gameSlot);
            }
        }

        return gameSlots.stream()
                .map(gameSlot -> {
                    GameSlotResponseDto gameSlotResponseDto = new GameSlotResponseDto();
                    gameSlotResponseDto.setSlotId(gameSlot.getId());
                    gameSlotResponseDto.setDate(gameSlot.getSlotDate());
                    gameSlotResponseDto.setStartTime(gameSlot.getStartTime());
                    gameSlotResponseDto.setEndTime(gameSlot.getEndTime());
                    gameSlotResponseDto.setMaxPlayers(gameSlot.getMaxPlayers());
                    gameSlotResponseDto.setGameName(gameSlot.getGame().getGameName());
                    return gameSlotResponseDto;
                }).toList();

    }

    public GameSlotResponseDto getGameSlotById(Long gameSlotId) {

        //I have to prevent returning slot that are in past and already started and registrations are closed

        GameSlot gameSlot = gameSlotRepository.findById(gameSlotId).orElseThrow(() -> new ResourceNotFoundException("Slot does not exist"));
        LocalTime now = LocalTime.now();
        LocalTime oneHourLater = now.plusHours(1);
        LocalTime slotStartTime = gameSlot.getStartTime();
        LocalTime slotEndTime = gameSlot.getEndTime();
        if(slotEndTime.isBefore(now) || slotStartTime.isBefore(oneHourLater) || now.isAfter(slotStartTime)) {
            throw new InvalidParameterException("Slot already started or registrations are closed");
        }

        GameSlotResponseDto gameSlotResponseDto = new GameSlotResponseDto();
        gameSlotResponseDto.setSlotId(gameSlot.getId());
        gameSlotResponseDto.setDate(gameSlot.getSlotDate());
        gameSlotResponseDto.setStartTime(gameSlot.getStartTime());
        gameSlotResponseDto.setEndTime(gameSlot.getEndTime());
        gameSlotResponseDto.setMaxPlayers(gameSlot.getMaxPlayers());
        gameSlotResponseDto.setGameName(gameSlot.getGame().getGameName());

        return gameSlotResponseDto;
    }



//    public void generateSlotsForPool() {
//        ConfigureGame configureGame = configureGameRepository.findByGameGName("Pool Table");
//        LocalTime startTime = configureGame.getStartTime();
//        LocalTime endTime = configureGame.getEndTime();
//        Duration duration = Duration.ofMinutes(configureGame.getSlotDuration());
//
//        while (startTime.isBefore(endTime)) {
//            GameSlot gameSlot = new GameSlot();
//            gameSlot.setStartTime(startTime);
//            gameSlot.setEndTime(startTime.plusMinutes(duration.toMinutes()));
//            gameSlot.setGame(configureGame.getGame());
//            gameSlot.setSlotDate(LocalDate.now());
//            gameSlot.setMaxPlayers(configureGame.getMaxPlayers());
//            gameSlotRepository.save(gameSlot);
//            startTime = startTime.plusMinutes(duration.toMinutes());
//        }
//    }
}
