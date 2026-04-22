package com.example.hrms.services.game;

import com.example.hrms.dtos.game.GameSlotResponseDto;
import com.example.hrms.dtos.game.RegisterSlotInterestDto;
import com.example.hrms.dtos.game.SlotRegistrationResponseDto;
import com.example.hrms.dtos.game.UserSlotStatusDto;
import com.example.hrms.dtos.travel.EmployeeWithTravelDto;
import com.example.hrms.entities.*;
import com.example.hrms.enums.SlotBookingStatus;
import com.example.hrms.enums.SlotRegistrationStatus;
import com.example.hrms.exceptions.DailyLimitExceededException;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.exceptions.TravelConflictException;
import com.example.hrms.repositories.*;
import com.example.hrms.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SlotRegistrationService {
    private final GameSlotRepository gameSlotRepository;
    private final GameRepository gameRepository;
    private final WeeklySlotCountRepository weeklySlotCountRepository;
    private  final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final SlotRegistrationRepository slotRegistrationRepository;
    private final EmployeeGameInterestRepository employeeGameInterestRepository;
    private final SlotBookingRepository slotBookingRepository;
    private final NotificationService notificationService;
    private final EmployeeTravelRepository employeeTravelRepository;
    private static final int MAX_DAILY_BOOKINGS = 3;

    public void registerInterest(Long slotId,
                                     Long bookedByUserId,
                                     RegisterSlotInterestDto dto) {

        GameSlot slot = gameSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (Boolean.TRUE.equals(slot.getIsFinalized())) {
            throw new IllegalStateException("Slot already finalized");
        }

        validateSlotTiming(slot);

        User user = userRepository.findById(bookedByUserId)
             .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Employee bookedBy = user.getEmployee();

            Set<Long> employeeIds = new HashSet<>();
            employeeIds.add(bookedBy.getId());

            if (dto.getEmployeeIds() != null) {
                employeeIds.addAll(dto.getEmployeeIds());
            }

        Map<Long, Employee> employeeMap = employeeRepository
                .findAllById(employeeIds)
                .stream()
                .collect(Collectors.toMap(Employee::getId, Function.identity()));

            LocalDate weekStart = LocalDate.now()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            List<Employee> conflictingEmployees = new ArrayList<>();
            for (Long employeeId : employeeIds) {

                Employee employee = employeeMap.get(employeeId);
                if (employee == null) {
                    throw new ResourceNotFoundException("Employee not found");
                }

                boolean isOnTravel = employeeTravelRepository
                        .existsTravelConflict(employee.getId(), slot.getSlotDate());

                if (isOnTravel) {
                    conflictingEmployees.add(employee);
                }
            }

        if (!conflictingEmployees.isEmpty()) {
            throw new TravelConflictException(conflictingEmployees);
        }

        List<Employee> limitExceededEmployees = new ArrayList<>();

        for (Long employeeId : employeeIds) {

            Employee employee = employeeMap.get(employeeId);
            if (employee == null) {
                throw new ResourceNotFoundException("Employee not found");
            }

            long activeRequests = slotRegistrationRepository
                    .countByEmployeeAndDateAndStatusIn(
                            employee.getId(),
                            slot.getSlotDate(),
                            List.of(
                                    SlotRegistrationStatus.PENDING,
                                    SlotRegistrationStatus.CONFIRMED
                            )
                    );

            if (activeRequests >= MAX_DAILY_BOOKINGS) {
                limitExceededEmployees.add(employee);
            }
        }

        if (!limitExceededEmployees.isEmpty()) {
            throw new DailyLimitExceededException(limitExceededEmployees);
        }

            for (Long employeeId : employeeIds) {

                Employee employee = employeeMap.get(employeeId);
                if (employee == null) {
                    throw new ResourceNotFoundException("Employee not found");
                }

                ensureEmployeeInterested(employee, slot.getGame());

                ensureNotAlreadyRegistered(employee, slot);

                //
                checkBookingConflicts(slot, employee);
                //

                WeeklySlotCount weeklySlotCount =
                        getOrCreateWeeklySlotCount(employee, slot.getGame(), weekStart);

                SlotRegistration registration = new SlotRegistration();
                registration.setEmployee(employee);
                registration.setSlot(slot);
                registration.setSlotCountAtRequest(weeklySlotCount.getSlotCount());
                registration.setStatus(SlotRegistrationStatus.PENDING);
                registration.setBookedBy(bookedBy);
                slotRegistrationRepository.save(registration);
                notificationService.notifyRegistrationMade(bookedBy, employee, slot);
            }
    }
    //
    public void cancelRegistrationOrBooking(Long slotId, Long bookedByUserId) {
        User user = userRepository.findById(bookedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee bookedBy = user.getEmployee();
        GameSlot gameSlot = gameSlotRepository.findById(slotId).orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        LocalDateTime slotStart = LocalDateTime.of(gameSlot.getSlotDate(), gameSlot.getStartTime());
        if(LocalDateTime.now().isAfter(slotStart)){
            throw new IllegalStateException("Slot has already started");
        }

        SlotBooking booking = slotBookingRepository.findByGameSlotAndEmployee(gameSlot, bookedBy);
        if(booking != null){
            if(booking.getStatus() == SlotBookingStatus.CANCELLED){
                return;
            }

            booking.setStatus(SlotBookingStatus.CANCELLED);
            notificationService.notifySlotBookingCancelled(bookedBy, bookedBy, gameSlot);
            return;
        }

        //SlotRegistration registration = slotRegistrationRepository.findByEmployeeAndSlot(bookedBy, gameSlot);
        SlotRegistration registration = slotRegistrationRepository.findByEmployeeAndSlotAndStatusNot(
                bookedBy, gameSlot, SlotRegistrationStatus.CANCELLED
        );
        if(registration != null){
            if(registration.getStatus() == SlotRegistrationStatus.CANCELLED){
                return;
            }

            registration.setStatus(SlotRegistrationStatus.CANCELLED);
            notificationService.notifySlotRegistrationCancelled(bookedBy, bookedBy, gameSlot);
            return;
        }

        throw new IllegalStateException("No active registration or booking found");

        //Three cases:
        /*
        * Employee cancels before Time
        * Employee cancels after 1HR mark
        * Employee cancels anytime after slot start(we must prevent any attempts for this)
        * */
//        if(validateSlotTimingForCancellation(gameSlot)){
//
//            //case when player cancels before
//            SlotRegistration slotRegistration = slotRegistrationRepository.findByEmployeeAndSlot(bookedBy, gameSlot);
//            if (slotRegistration != null) {
//                slotRegistration.setStatus(SlotRegistrationStatus.CANCELLED);
//            }
//        }else {
//            //case when player cancels after
//            SlotBooking slotBooking = slotBookingRepository.findByGameSlotAndEmployee(gameSlot, bookedBy);
//            if (slotBooking != null) {
//                slotBooking.setStatus(SlotBookingStatus.CANCELLED);
//            }
//        }



    }

//    private boolean validateSlotTimingForCancellation(GameSlot slot) {
//
//        LocalDateTime slotStart =
//                LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
//
//        return !LocalDateTime.now().isAfter(slotStart.minusHours(1));
//    }

    public List<SlotRegistrationResponseDto> getEmployeeRegistrations(Long userId, Long gameId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = user.getEmployee();
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        List<SlotRegistration> slotRegistrations = slotRegistrationRepository.findActiveRegistrartionForToday(
                employee,LocalDate.now(),game.getId()
        );

        return slotRegistrations.stream()
                .map(reg -> mapToDto(reg, employee))
                .toList();
    }

    private SlotRegistrationResponseDto mapToDto(SlotRegistration registration, Employee currentEmployee) {

        GameSlot slot = registration.getSlot();

        SlotRegistrationResponseDto dto =
                new SlotRegistrationResponseDto();

        Employee bookedBy = registration.getBookedBy();
        String bookedByName;

        if (bookedBy.getId().equals(currentEmployee.getId())) {
            bookedByName = "You";
        } else {
            bookedByName = bookedBy.getFirstName() + " " + bookedBy.getLastName();
        }

        dto.setSlotRegistrationId(registration.getId());
        dto.setSlotId(slot.getId());
        dto.setDate(slot.getSlotDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setMaxPlayers(slot.getMaxPlayers());
        dto.setGameName(slot.getGame().getGameName());
        dto.setBookedBy(bookedByName);
        dto.setBookedById(bookedBy.getId());
        return dto;
    }
    //
    private void validateSlotTiming(GameSlot slot) {

            LocalDateTime slotStart =
                    LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());

            if (LocalDateTime.now().isAfter(slotStart.minusHours(1))) {
                throw new IllegalStateException("Cannot register within 1 hour of slot start");
            }
    }

        private void ensureNotAlreadyRegistered(Employee employee, GameSlot slot) {
            boolean exists = slotRegistrationRepository
                    .existsByEmployeeAndSlotAndStatusNot(employee, slot, SlotRegistrationStatus.CANCELLED);

            if (exists) {
                throw new IllegalArgumentException(
                        "Employee already registered for this slot");
            }
        }

        private void ensureEmployeeInterested(Employee employee, Game game) {
            boolean interested = employeeGameInterestRepository
                    .existsByEmployeeAndGame(employee, game);

            if (!interested) {
                throw new IllegalStateException(
                        "Employee is not interested in this game");
            }
        }

        private WeeklySlotCount getOrCreateWeeklySlotCount(Employee employee,
                                                           Game game,
                                                           LocalDate weekStart) {

            WeeklySlotCount weeklySlotCount =
                    weeklySlotCountRepository
                            .findByEmployeeAndGameAndWeekStartDate(employee, game, weekStart);

            if (weeklySlotCount == null) {
                weeklySlotCount = new WeeklySlotCount();
                weeklySlotCount.setEmployee(employee);
                weeklySlotCount.setGame(game);
                weeklySlotCount.setWeekStartDate(weekStart);
                weeklySlotCount.setSlotCount(0);
                weeklySlotCountRepository.save(weeklySlotCount);
            }

            return weeklySlotCount;
        }

        //block booking if the timing of slot being registered conflicts with any existing booking
        private void checkBookingConflicts(GameSlot gameSlot, Employee employee) {
            LocalDate slotDate = gameSlot.getSlotDate();
            LocalTime slotStartTime = gameSlot.getStartTime();
            LocalTime slotEndTime = gameSlot.getEndTime();

            List<SlotBooking> slotBookings = slotBookingRepository.findByEmployeeAndDate(employee, slotDate);
            for (SlotBooking slotBooking : slotBookings) {
                if(slotBooking.getGameSlot().getStartTime().isBefore(slotEndTime) &&
                    slotBooking.getGameSlot().getEndTime().isAfter(slotStartTime)){
                    throw new IllegalStateException("One or more employee's existing slot booking timings conflict with this slot");
                }
            }
        }

    public UserSlotStatusDto getUserSlotStatus(Long userId, Long slotId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Employee employee = user.getEmployee();

        GameSlot slot = gameSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        // ✅ Travel check
        boolean isOnTravel = employeeTravelRepository
                .existsTravelConflict(employee.getId(), slot.getSlotDate());

        // ✅ Limit check
        long activeRequests = slotRegistrationRepository
                .countByEmployeeAndDateAndStatusIn(
                        employee.getId(),
                        slot.getSlotDate(),
                        List.of(
                                SlotRegistrationStatus.PENDING,
                                SlotRegistrationStatus.CONFIRMED
                        )
                );

        boolean isLimitReached = activeRequests >= MAX_DAILY_BOOKINGS;

        boolean isRegistered = slotRegistrationRepository
                .existsByEmployeeAndSlotAndStatusIn(
                        employee,
                        slot,
                        List.of(
                                SlotRegistrationStatus.PENDING,
                                SlotRegistrationStatus.CONFIRMED
                        )
                );

        return new UserSlotStatusDto(isOnTravel, isLimitReached, isRegistered);
    }

//    public Boolean isCurrentUserOnTravel(Long userId, Long slotId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        Employee employee = user.getEmployee();
//
//        GameSlot slot = gameSlotRepository.findById(slotId)
//                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
//
//        return employeeTravelRepository
//                .existsTravelConflict(employee.getId(), slot.getSlotDate());
//    }
//    public List<EmployeeWithTravelDto> getEmployeesForSlot(Long slotId) {
//
//        GameSlot slot = gameSlotRepository.findById(slotId)
//                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
//
//        return employeeRepository.findEmployeesWithTravelStatus(slot.getSlotDate());
//    }

        //
    }

//    public void registerInterest(Long slotId, Long userId, RegisterSlotInterestDto dto) {
//        GameSlot slot = gameSlotRepository.findById(slotId)
//                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
//
//        if(slot.getIsFinalized()){
//            throw new TimeOutException("Slot is finalized");
//        }
//
//        LocalTime currentTime = LocalTime.now();
//        LocalTime closeTime = slot.getStartTime().minusHours(1);
//
//        if(currentTime.isAfter(closeTime)) {
//            throw new TimeOutException("Slot has been closed");
//        }
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        Employee bookedBy = user.getEmployee();
//
//
//        LocalDate weekStart = LocalDate.now()
//                .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
//
//        Set<Long> employeeIds = new HashSet<>();
//
//        if (dto.getEmployeeIds() != null) {
//            employeeIds.addAll(dto.getEmployeeIds());
//        }
//
//        employeeIds.add(bookedBy.getId());
//
//        for (Long employeeId : employeeIds) {
//
//            Employee employee = employeeRepository.findById(employeeId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
//
//            WeeklySlotCount weeklySlotCount =
//                    weeklySlotCountRepository
//                            .findByEmployee_IdAndWeekStartDateAndGame_Id(
//                                    employeeId, weekStart, slot.getGame().getId());
//
//            if (weeklySlotCount == null) {
//                weeklySlotCount = new WeeklySlotCount();
//                weeklySlotCount.setWeekStartDate(weekStart);
//                weeklySlotCount.setSlotCount(0);
//                weeklySlotCount.setGame(slot.getGame());
//                weeklySlotCount.setEmployee(employee);
//                weeklySlotCountRepository.save(weeklySlotCount);
//            }
//
//            if (slotRegistrationRepository
//                    .findByEmployeeAndSlot(employee, slot) != null) {
//                throw new IllegalArgumentException("Employee already registered");
//            }
//
//            SlotRegistration registration = new SlotRegistration();
//            registration.setSlot(slot);
//            registration.setEmployee(employee);
//            registration.setSlotCountAtRequest(weeklySlotCount.getSlotCount());
//
//            slotRegistrationRepository.save(registration);
//        }
//    }

