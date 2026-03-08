package com.example.hrms.services.game;

import com.example.hrms.dtos.game.GameSlotResponseDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.GameSlot;
import com.example.hrms.entities.SlotBooking;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.SlotBookingRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotBookingService {
    private final SlotBookingRepository slotBookingRepository;
    private final UserRepository userRepository;

    public List<GameSlotResponseDto> getUpcomingBookings(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        //Employee employee = user.getEmployee();

        List<SlotBooking> slotBookings = slotBookingRepository.findByEmployeeAndStatusIsConfirmed(user.getEmployee())
                .stream().filter(slotBooking -> slotBooking.getGameSlot().getSlotDate().isEqual(LocalDate.now()) && slotBooking.getGameSlot().getStartTime().isAfter(LocalTime.now()))
                .toList();

        return slotBookings.stream()
                .map(slotBooking -> {
                    GameSlotResponseDto dto = new GameSlotResponseDto();
                    GameSlot gameSlot = slotBooking.getGameSlot();
                    dto.setSlotId(gameSlot.getId());
                    dto.setStartTime(gameSlot.getStartTime());
                    dto.setEndTime(gameSlot.getEndTime());
                    dto.setMaxPlayers(gameSlot.getMaxPlayers());
                    dto.setGameName(gameSlot.getGame().getGameName());
                    dto.setDate(gameSlot.getSlotDate());
                    return dto;
                }).toList();
    }

    public List<GameSlotResponseDto> getCancelledBookings(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<SlotBooking> slotBookings = slotBookingRepository.findByEmployeeAndStatusIsCancelled(user.getEmployee());
        return slotBookings.stream()
                .map(slotBooking -> {
                    GameSlotResponseDto dto = new GameSlotResponseDto();
                    GameSlot gameSlot = slotBooking.getGameSlot();
                    dto.setSlotId(gameSlot.getId());
                    dto.setStartTime(gameSlot.getStartTime());
                    dto.setEndTime(gameSlot.getEndTime());
                    dto.setMaxPlayers(gameSlot.getMaxPlayers());
                    dto.setGameName(gameSlot.getGame().getGameName());
                    dto.setDate(gameSlot.getSlotDate());
                    return dto;
                }).toList();
    }

    public List<GameSlotResponseDto> getCompletedBookings(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<SlotBooking> slotBookings = slotBookingRepository.findByEmployeeAndStatusIsConfirmed(user.getEmployee())
                .stream().filter(slotBooking -> slotBooking.getGameSlot().getSlotDate().isBefore(LocalDate.now()) || slotBooking.getGameSlot().getEndTime().isBefore(LocalTime.now()))
                .toList();

        return slotBookings.stream()
                .map(slotBooking -> {
                    GameSlotResponseDto dto = new GameSlotResponseDto();
                    GameSlot gameSlot = slotBooking.getGameSlot();
                    dto.setSlotId(gameSlot.getId());
                    dto.setStartTime(gameSlot.getStartTime());
                    dto.setEndTime(gameSlot.getEndTime());
                    dto.setMaxPlayers(gameSlot.getMaxPlayers());
                    dto.setGameName(gameSlot.getGame().getGameName());
                    dto.setDate(gameSlot.getSlotDate());
                    return dto;
                }).toList();
    }

}
