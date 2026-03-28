package com.example.hrms.controllers;

import com.example.hrms.dtos.travel.*;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.travel.TravelPlanService;
import com.example.hrms.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
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

    @PreAuthorize("hasRole('HR')")
    @DeleteMapping("/delete/{travelPlanId}")
    public ResponseEntity<Void> deleteTravelPlan(@PathVariable("travelPlanId") Long travelPlanId, @AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {
        travelPlanService.deleteTravelPlan(travelPlanId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('HR')")
    @PatchMapping("/update/{Id}")
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

    @GetMapping("/filter")
    public ResponseEntity<List<TravelPlanResponseDto>> getAllTravelPlans(@RequestParam(defaultValue = "ACTIVE") String status){
        List<TravelPlanResponseDto> travelPlanResponseDtos = travelPlanService.getAllTravelPlansByStatus(status);
        return ResponseEntity.ok(travelPlanResponseDtos);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<EmployeeDto>> getTravelPlanParticipants(@PathVariable("id") Long id){
        List<EmployeeDto> employeeDtos = travelPlanService.getTravelPlanParticipants(id);
        return ResponseEntity.ok(employeeDtos);
    }

    @GetMapping("/employee")
    public ResponseEntity<List<TravelPlanResponseDto>> getTravelPlansByEmployee(@AuthenticationPrincipal MyUserDetails userDetails){
        Long userId = userDetails.getId();
        List<TravelPlanResponseDto> travelPlanResponseDtos = travelPlanService.getTravelPlansByEmployee(userId);
        return ResponseEntity.ok(travelPlanResponseDtos);

    }

    @GetMapping("/employee/filter")
    public ResponseEntity<List<TravelPlanResponseDto>> getTravelPlansByEmployeeAndStatus(@RequestParam(defaultValue = "ACTIVE") String status,@AuthenticationPrincipal MyUserDetails userDetails){
        List<TravelPlanResponseDto> travelPlanResponseDtos = travelPlanService.getTravelPlansByEmployeeAndStatus(userDetails.getId(), status);
        return ResponseEntity.ok(travelPlanResponseDtos);

    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<TravelPlanDetailDto> getTravelPlanDetailById(@PathVariable("id") Long id){
        TravelPlanDetailDto travelPlanResponseDto = travelPlanService.getTravelPlanDetailById(id);
        return ResponseEntity.ok(travelPlanResponseDto);
    }

    @GetMapping("/employee/{employee-id}")
    public ResponseEntity<List<TravelPlanResponseDto>> getTravelPlansByEmployeeId(@PathVariable("employee-id") Long employeeId){
        List<TravelPlanResponseDto> travelPlanResponseDtos = travelPlanService.getTravelPlansByEmployeeId(employeeId);
        return ResponseEntity.ok(travelPlanResponseDtos);

    }

    @GetMapping("/employee/{employee-id}/filter")
    public ResponseEntity<List<TravelPlanResponseDto>> getTravelPlansByEmployeeAndStatus(@PathVariable("employee-id") Long employeeId, @RequestParam(defaultValue = "ACTIVE") String status){
        List<TravelPlanResponseDto> travelPlanResponseDtos = travelPlanService.getTravelPlansByEmployeeIdAndStatus(employeeId, status);
        return ResponseEntity.ok(travelPlanResponseDtos);

    }

}
