package com.example.hrms.controllers.game;

import com.example.hrms.dtos.game.GameSlotResponseDto;
import com.example.hrms.dtos.game.SlotDetailDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.game.SlotBookingService;
import jdk.jfr.Registered;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Registered
@RequiredArgsConstructor
@RestController
@RequestMapping("/booking")
public class SlotBookingController {
    private final SlotBookingService slotBookingService;

    @GetMapping("/upcoming")
    public ResponseEntity<List<GameSlotResponseDto>> getUpcomingBookings(@AuthenticationPrincipal MyUserDetails userDetails) {
        return new ResponseEntity<>(slotBookingService.getUpcomingBookings(userDetails.getId()), HttpStatus.OK);
    }

    @GetMapping("/cancelled")
    public ResponseEntity<List<GameSlotResponseDto>> getCancelledBookings(@AuthenticationPrincipal MyUserDetails userDetails) {
        return new ResponseEntity<>(slotBookingService.getCancelledBookings(userDetails.getId()), HttpStatus.OK);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<GameSlotResponseDto>> getCompletedBookings(@AuthenticationPrincipal MyUserDetails userDetails) {
        return new ResponseEntity<>(slotBookingService.getCompletedBookings(userDetails.getId()), HttpStatus.OK);
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<SlotDetailDto> getSlotDetails(@PathVariable Long slotId) {
        SlotDetailDto response = slotBookingService.getSlotDetails(slotId);
        return ResponseEntity.ok(response);
    }
}
