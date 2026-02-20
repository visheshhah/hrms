package com.example.hrms.controllers;

import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.dtos.travel.TravelPlanDto;
import com.example.hrms.dtos.travel.TravelPlanResponseDto;
import com.example.hrms.dtos.travel.UpdateTravelPlanDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.travel.TravelPlanService;
import com.example.hrms.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/travel")
@RequiredArgsConstructor
public class TravelPlanController {
    private final TravelPlanService travelPlanService;

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/create")
    public ResponseEntity<TravelPlanResponseDto> createTravelPlan(@Valid @RequestBody TravelPlanDto travelPlanDto, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long creatorId = userDetails.getId();
        TravelPlanResponseDto travelPlanResponseDto = travelPlanService.createTravelPlan(travelPlanDto, creatorId);
        return ResponseEntity.ok(travelPlanResponseDto);
    }

    //TODO Pending Testing
    @PreAuthorize("hasRole('HR')")
    @PostMapping("/update/{Id}")
    public ResponseEntity<TravelPlanResponseDto> updateTravelPlan(@PathVariable("Id") Long id, @RequestBody UpdateTravelPlanDto updateTravelPlanDto, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long creatorId = userDetails.getId();
        TravelPlanResponseDto travelPlanResponseDto = travelPlanService.updateTravelPlan(id, updateTravelPlanDto, creatorId);
        return ResponseEntity.ok(travelPlanResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPlanResponseDto> getTravelPlanById(@PathVariable("id") Long id){
        TravelPlanResponseDto travelPlanResponseDto = travelPlanService.getTravelPlanById(id);
        return ResponseEntity.ok(travelPlanResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<TravelPlanResponseDto>> getAllTravelPlans(){
        List<TravelPlanResponseDto> travelPlanResponseDtos = travelPlanService.getAllTravelPlans();
        return ResponseEntity.ok(travelPlanResponseDtos);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<EmployeeDto>> getTravelPlanParticipants(@PathVariable("id") Long id){
        List<EmployeeDto> employeeDtos = travelPlanService.getTravelPlanParticipants(id);
        return ResponseEntity.ok(employeeDtos);
    }

    //TODO
    @GetMapping("/employee")
    public ResponseEntity<List<TravelPlanResponseDto>> getTravelPlansByEmployee(@AuthenticationPrincipal MyUserDetails userDetails){
        Long userId = userDetails.getId();
        List<TravelPlanResponseDto> travelPlanResponseDtos = travelPlanService.getTravelPlansByEmployeeId(userId);
        return ResponseEntity.ok(travelPlanResponseDtos);

    }



}
