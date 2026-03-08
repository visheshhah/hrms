package com.example.hrms.scheduler;

import com.example.hrms.entities.*;
import com.example.hrms.enums.SlotBookingStatus;
import com.example.hrms.enums.SlotRegistrationStatus;
import com.example.hrms.repositories.GameSlotRepository;
import com.example.hrms.repositories.SlotBookingRepository;
import com.example.hrms.repositories.SlotRegistrationRepository;
import com.example.hrms.repositories.WeeklySlotCountRepository;
import com.example.hrms.services.game.SlotFinalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SlotFinalizationScheduler {
    private final GameSlotRepository gameSlotRepository;
    private final SlotFinalizationService slotFinalizationService;

    @Scheduled(fixedRate = 60000)
    //@Scheduled(cron = "0 * * * * *")
    public void finalizeEligibleSlots() {

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<GameSlot> slots =
                gameSlotRepository.findBySlotDateAndIsFinalizedFalse(today);

        for (GameSlot slot : slots) {

            LocalDateTime slotStart =
                    LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());

            if (slotStart.isAfter(now) && slotStart.isBefore(oneHourLater)) {
                slotFinalizationService.finalizeSlot(slot);
            }
        }
    }

}
