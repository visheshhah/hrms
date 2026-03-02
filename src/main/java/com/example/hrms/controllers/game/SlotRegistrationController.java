package com.example.hrms.controllers.game;

import com.example.hrms.dtos.game.GameSlotResponseDto;
import com.example.hrms.dtos.game.RegisterSlotInterestDto;
import com.example.hrms.dtos.game.SlotRegistrationResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.game.SlotRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slot")
public class SlotRegistrationController {
    private final SlotRegistrationService slotRegistrationService;

    @PostMapping("/register/{slotId}")
    public void register(@PathVariable("slotId") Long slotId, @AuthenticationPrincipal MyUserDetails userDetails, @RequestBody RegisterSlotInterestDto registerSlotInterestDto) {
        slotRegistrationService.registerInterest(slotId, userDetails.getId(), registerSlotInterestDto);
    }

    //
    @PatchMapping("/cancel/{slotId}")
    public void cancel(@PathVariable("slotId") Long slotId,  @AuthenticationPrincipal MyUserDetails userDetails) {
        slotRegistrationService.cancelRegistrationOrBooking(slotId, userDetails.getId());
    }

    @GetMapping("/{gameId}/registrations")
    public ResponseEntity<List<SlotRegistrationResponseDto>> getEmployeeRegistration(@PathVariable("gameId") Long gameId, @AuthenticationPrincipal MyUserDetails userDetails) {
        return new ResponseEntity<>(slotRegistrationService.getEmployeeRegistrations(userDetails.getId(), gameId), HttpStatus.OK);
    }
    //
}
