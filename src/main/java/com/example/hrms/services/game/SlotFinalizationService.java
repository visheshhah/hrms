package com.example.hrms.services.game;

import com.example.hrms.entities.*;
import com.example.hrms.enums.SlotRegistrationStatus;
import com.example.hrms.repositories.GameSlotRepository;
import com.example.hrms.repositories.SlotBookingRepository;
import com.example.hrms.repositories.SlotRegistrationRepository;
import com.example.hrms.repositories.WeeklySlotCountRepository;
import com.example.hrms.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotFinalizationService {

    private final GameSlotRepository gameSlotRepository;
    private final SlotBookingRepository slotBookingRepository;
    private final SlotRegistrationRepository slotRegistrationRepository;
    private final WeeklySlotCountRepository weeklySlotCountRepository;
    private final NotificationService  notificationService;


    @Transactional
    public void finalizeSlot(GameSlot slot) {

        GameSlot managedSlot = gameSlotRepository.findById(slot.getId())
                .orElseThrow();

        if (Boolean.TRUE.equals(managedSlot.getIsFinalized())) {
            return;
        }

        List<SlotRegistration> registrations =
                slotRegistrationRepository
                        .findPendingRegistrationsOrdered(managedSlot);

        if (registrations.isEmpty()) {
            managedSlot.setIsFinalized(true);
            return;
        }

        int capacity = managedSlot.getMaxPlayers();
        //
         List<SlotRegistration> selected = new ArrayList<>();
         for (SlotRegistration r : registrations) {
             if(selected.size() == capacity){
                 break;
             }
             boolean hasConflicts = checkBookingConflicts(managedSlot, r.getEmployee());

             if(!hasConflicts){
                 selected.add(r);
             }
         }

        //
//        List<SlotRegistration> selected =
//                registrations.stream()
//                        .limit(capacity)
//                        .toList();

        for (SlotRegistration registration : selected) {

            boolean alreadyBooked =
                    slotBookingRepository.existsByGameSlotAndEmployee(
                            managedSlot,
                            registration.getEmployee()
                    );

            if (!alreadyBooked) {

                SlotBooking booking = new SlotBooking();
                booking.setGameSlot(managedSlot);
                booking.setEmployee(registration.getEmployee());
                slotBookingRepository.save(booking);

                incrementWeeklySlotCount(registration);
                notificationService.notifySlotBooking(registration.getEmployee(), booking, managedSlot);
            }

            if (registration.getStatus() != SlotRegistrationStatus.CONFIRMED) {
                registration.setStatus(SlotRegistrationStatus.CONFIRMED);
            }
        }

        for (SlotRegistration registration : registrations) {

            if (!selected.contains(registration)
                    && registration.getStatus() == SlotRegistrationStatus.PENDING) {

                registration.setStatus(SlotRegistrationStatus.REJECTED);
                notificationService.notifyRegistrationRejected(registration.getEmployee(), managedSlot);
            }
        }

        managedSlot.setIsFinalized(true);
    }


    private void incrementWeeklySlotCount(SlotRegistration registration) {

        Employee employee = registration.getEmployee();
        Game game = registration.getSlot().getGame();

        LocalDate weekStart = LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters
                        .previousOrSame(DayOfWeek.MONDAY));

        WeeklySlotCount weeklySlotCount =
                weeklySlotCountRepository
                        .findByEmployeeAndGameAndWeekStartDate(
                                employee, game, weekStart);
        if(weeklySlotCount == null){
            weeklySlotCount = new WeeklySlotCount();
            weeklySlotCount.setEmployee(employee);
            weeklySlotCount.setGame(game);
            weeklySlotCount.setWeekStartDate(weekStart);
            weeklySlotCount.setSlotCount(1);
        }else{
            weeklySlotCount.setSlotCount(
                    weeklySlotCount.getSlotCount() + 1
            );
        }
        weeklySlotCountRepository.save(weeklySlotCount);

//        if (weeklySlotCount != null) {
//            weeklySlotCount.setSlotCount(
//                    weeklySlotCount.getSlotCount() + 1
//            );
//            weeklySlotCountRepository.save(weeklySlotCount);
//        }
    }

    private boolean checkBookingConflicts(GameSlot gameSlot, Employee employee) {
        LocalDate slotDate = gameSlot.getSlotDate();
        LocalTime slotStartTime = gameSlot.getStartTime();
        LocalTime slotEndTime = gameSlot.getEndTime();

        List<SlotBooking> slotBookings = slotBookingRepository.findByEmployeeAndDate(employee, slotDate);
        for (SlotBooking slotBooking : slotBookings) {
            if(slotBooking.getGameSlot().getStartTime().isBefore(slotEndTime) &&
                    slotBooking.getGameSlot().getEndTime().isAfter(slotStartTime)){
                return true;
            }
        }
        return false;
    }

}
