package com.example.hrms.services.game;

import com.example.hrms.dtos.game.EmployeeGameInterestResponseDto;
import com.example.hrms.dtos.game.UpdateEmployeeGameInterestDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.EmployeeGameInterest;
import com.example.hrms.entities.Game;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeGameInterestRepository;
import com.example.hrms.repositories.GameRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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
}
