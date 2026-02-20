package com.example.hrms.controllers;

import com.example.hrms.dtos.expense.HrDecisionDto;
import com.example.hrms.dtos.expense.HrDecisionResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.MyUserDetailsService;
import com.example.hrms.services.expense.HrExpenseService;
import com.example.hrms.utils.JwtUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hr/expense/decision")
public class HrExpenseController {
    private final HrExpenseService hrExpenseService;

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/approve")
    public ResponseEntity<HrDecisionResponseDto> approve(@RequestBody HrDecisionDto hrDecisionDto, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long hrId = userDetails.getId();
        HrDecisionResponseDto  hrDecisionResponseDto = hrExpenseService.approveExpense(hrDecisionDto, hrId);
        return ResponseEntity.ok(hrDecisionResponseDto);
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/reject")
    public ResponseEntity<HrDecisionResponseDto> reject(@RequestBody HrDecisionDto hrDecisionDto, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long hrId = userDetails.getId();
        HrDecisionResponseDto  hrDecisionResponseDto = hrExpenseService.rejectExpense(hrDecisionDto, hrId);
        return ResponseEntity.ok(hrDecisionResponseDto);
    }

}
