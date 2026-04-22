package com.example.hrms.services.game;

import com.example.hrms.dtos.game.EmployeeGameInterestResponseDto;
import com.example.hrms.dtos.game.GameTabDto;
import com.example.hrms.dtos.game.UpdateEmployeeGameInterestDto;
import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.entities.*;
import com.example.hrms.enums.SlotRegistrationStatus;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeGameInterestService {
    private final EmployeeGameInterestRepository employeeGameInterestRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameSlotRepository gameslotRepository;
    private final ModelMapper modelMapper;
    private final EmployeeTravelRepository employeeTravelRepository;
    private final SlotRegistrationRepository slotRegistrationRepository;
    private static final int MAX_DAILY_BOOKINGS = 3;

    public void updateEmployeeGameInterest(Long userId, UpdateEmployeeGameInterestDto dto)
    {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = user.getEmployee();

        List<EmployeeGameInterest> existingInterests =
                employeeGameInterestRepository.findByEmployee(employee);

        Set<Long> existingGameIds = existingInterests.stream()
                .map(interest -> interest.getGame().getId())
                .collect(Collectors.toSet());

        Set<Long> requestedGameIds = dto.getGameIds() == null
                ? Set.of()
                : new HashSet<>(dto.getGameIds());

        Set<Long> toAdd = new HashSet<>(requestedGameIds);
        toAdd.removeAll(existingGameIds);

        Set<Long> toRemove = new HashSet<>(existingGameIds);
        toRemove.removeAll(requestedGameIds);

        if (!toRemove.isEmpty()) {
            employeeGameInterestRepository.deleteByEmployeeAndGameIdIn(employee, toRemove);
        }

        if (!toAdd.isEmpty()) {
            List<Game> gamesToAdd = gameRepository.findAllById(toAdd);

            for (Game game : gamesToAdd) {
                EmployeeGameInterest interest = new EmployeeGameInterest();
                interest.setEmployee(employee);
                interest.setGame(game);
                employeeGameInterestRepository.save(interest);
            }
        }
    }

    public List<EmployeeGameInterestResponseDto> getEmployeeGameInterestResponseDtos(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return employeeGameInterestRepository.findByEmployee(user.getEmployee())
                .stream()
                .map(gameInterest -> {
                    EmployeeGameInterestResponseDto employeeGameInterestResponseDto = new EmployeeGameInterestResponseDto();
                    employeeGameInterestResponseDto.setGameId(gameInterest.getGame().getId());
                    employeeGameInterestResponseDto.setGameName(gameInterest.getGame().getGameName());
                    return employeeGameInterestResponseDto;
                })
                .toList();
    }

    public List<EmployeeDto> getInterestedPlayers(Long userId, Long gameSlotId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = user.getEmployee();
        GameSlot gameSlot = gameslotRepository.findById(gameSlotId).orElseThrow(() -> new ResourceNotFoundException("Game slot not found"));
        Game game = gameSlot.getGame();
        List<EmployeeGameInterest> employeeGameInterests = employeeGameInterestRepository.findByGame(game);
        List<Employee> employees = employeeGameInterests.stream()
                .map(EmployeeGameInterest::getEmployee)
                .filter(e -> !e.getId().equals(employee.getId()))
                .toList();
        return employees.stream()
                .map(e -> {
                    EmployeeDto dto = modelMapper.map(e, EmployeeDto.class);

                    boolean isOnTravel = employeeTravelRepository
                            .existsTravelConflict(e.getId(), gameSlot.getSlotDate());

                    dto.setIsOnTravel(isOnTravel);
                    long activeRequests = slotRegistrationRepository
                            .countByEmployeeAndDateAndStatusIn(
                                    e.getId(),
                                    gameSlot.getSlotDate(),
                                    List.of(
                                            SlotRegistrationStatus.PENDING,
                                            SlotRegistrationStatus.CONFIRMED
                                    )
                            );

                    boolean isLimitReached = activeRequests >= MAX_DAILY_BOOKINGS;

                    dto.setIsLimitReached(isLimitReached);

                    boolean isAlreadyRegistered = slotRegistrationRepository
                            .existsByEmployeeAndSlotAndStatusIn(
                                    e,
                                    gameSlot,
                                    List.of(
                                            SlotRegistrationStatus.PENDING,
                                            SlotRegistrationStatus.CONFIRMED
                                    )
                            );
                    dto.setIsAlreadyRegistered(isAlreadyRegistered);
                    return dto;
                })
                .toList();
    }

    public List<GameTabDto> getEmployeeGameTabs(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = user.getEmployee();

        List<EmployeeGameInterest> interests =
                employeeGameInterestRepository.findByEmployee(employee);

        return interests.stream()
                .map(interest -> {
                    Game game = interest.getGame();

                    return new GameTabDto(
                            game.getGameName(),
                            game.getId()
                    );
                })
                .toList();
    }
}
