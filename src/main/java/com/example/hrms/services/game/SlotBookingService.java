package com.example.hrms.services.game;

import com.example.hrms.dtos.game.GameSlotResponseDto;
import com.example.hrms.dtos.game.ParticipantsDto;
import com.example.hrms.dtos.game.SlotDetailDto;
import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.GameSlot;
import com.example.hrms.entities.SlotBooking;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.GameSlotRepository;
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
    private final GameSlotRepository  gameSlotRepository;

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

    public SlotDetailDto getSlotDetails(Long slotId) {
        GameSlot slot = gameSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        List<SlotBooking> bookings = slotBookingRepository.findByGameSlotId(slotId);

        List<ParticipantsDto> participants = bookings.stream()
                .map(sb -> {
                    Employee e = sb.getEmployee();

                    ParticipantsDto dto = new ParticipantsDto();
                    dto.setEmployeeId(e.getId());
                    dto.setName(e.getFirstName() + " " + e.getLastName());
                    dto.setDepartment(e.getDepartment().getDepartmentName());
                    dto.setStatus(sb.getStatus());

                    return dto;
                })
                .toList();

        SlotDetailDto dto = new SlotDetailDto();
        dto.setSlotId(slot.getId());
        dto.setGameName(slot.getGame().getGameName());
        dto.setDate(slot.getSlotDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setMaxPlayers(slot.getMaxPlayers());
        dto.setParticipants(participants);

        return dto;
    }
}
