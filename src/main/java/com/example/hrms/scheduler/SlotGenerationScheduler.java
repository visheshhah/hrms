package com.example.hrms.scheduler;

import com.example.hrms.entities.ConfigureGame;
import com.example.hrms.entities.GameSlot;
import com.example.hrms.repositories.ConfigureGameRepository;
import com.example.hrms.repositories.GameRepository;
import com.example.hrms.repositories.GameSlotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SlotGenerationScheduler {
    private final GameSlotRepository gameSlotRepository;
    private final ConfigureGameRepository configureGameRepository;
    private final GameRepository gameRepository;

    //@Scheduled(cron = "0 0 0 * * *")
    //@Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void generateSlots() {

        LocalDate targetDate = LocalDate.now();

        List<ConfigureGame> configs = configureGameRepository.findAll();

        for (ConfigureGame config : configs) {

            if (gameSlotRepository.existsByGameAndSlotDate(
                    config.getGame(), targetDate)) {
                continue;
            }

            generateSlotsForGame(config, targetDate);
        }
    }

    private void generateSlotsForGame(ConfigureGame config,
                                      LocalDate date) {

        LocalTime start = config.getStartTime();
        LocalTime end = config.getEndTime();
        Integer duration = config.getSlotDuration();

        while (start.plusMinutes(duration).isBefore(end) ||
                start.plusMinutes(duration).equals(end)) {

            GameSlot slot = new GameSlot();
            slot.setGame(config.getGame());
            slot.setSlotDate(date);
            slot.setStartTime(start);
            slot.setEndTime(start.plusMinutes(duration));
            slot.setMaxPlayers(config.getMaxPlayers());
            slot.setIsFinalized(false);

            gameSlotRepository.save(slot);

            start = start.plusMinutes(duration);
        }
    }

}
