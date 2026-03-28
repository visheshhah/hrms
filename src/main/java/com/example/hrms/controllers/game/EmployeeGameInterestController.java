package com.example.hrms.controllers.game;

import com.example.hrms.dtos.game.EmployeeGameInterestResponseDto;
import com.example.hrms.dtos.game.GameTabDto;
import com.example.hrms.dtos.game.UpdateEmployeeGameInterestDto;
import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.game.EmployeeGameInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game-interest")
public class EmployeeGameInterestController {

    private final EmployeeGameInterestService employeeGameInterestService;

    @PutMapping
    public ResponseEntity<Void> updateEmployeeInterest(@RequestBody UpdateEmployeeGameInterestDto updateEmployeeGameInterestDto, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        employeeGameInterestService.updateEmployeeGameInterest(myUserDetails.getId(), updateEmployeeGameInterestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<EmployeeGameInterestResponseDto>> getEmplyeeInterests(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        List<EmployeeGameInterestResponseDto> dtos = employeeGameInterestService.getEmployeeGameInterestResponseDtos(myUserDetails.getId());
        return ResponseEntity.ok().body(dtos);
    }

    @GetMapping("/{game-slot-id}/employees")
    public ResponseEntity<List<EmployeeDto>> getInterestedEmployees(@PathVariable("game-slot-id") Long slotId,@AuthenticationPrincipal MyUserDetails myUserDetails) {
        List<EmployeeDto> dtos = employeeGameInterestService.getInterestedPlayers(myUserDetails.getId(), slotId);
        return ResponseEntity.ok().body(dtos);
    }

    @GetMapping("/tabs")
    public ResponseEntity<List<GameTabDto>> getGameTabs(@AuthenticationPrincipal MyUserDetails myUserDetails) {
        return new ResponseEntity<>(employeeGameInterestService.getEmployeeGameTabs(myUserDetails.getId()), HttpStatus.OK);
    }
}
