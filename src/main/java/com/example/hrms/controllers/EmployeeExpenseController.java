package com.example.hrms.controllers;

import com.example.hrms.dtos.expense.EmployeeExpenseResponseDto;
import com.example.hrms.dtos.expense.SubmitExpenseDto;
import com.example.hrms.dtos.file.FileResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.expense.EmployeeExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expense")
public class EmployeeExpenseController {
    private final EmployeeExpenseService employeeExpenseService;


    //@PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{travel-plan-id}")
    public ResponseEntity<List<EmployeeExpenseResponseDto>> getEmployeeExpenses(@PathVariable("travel-plan-id") Long travelPlanId, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long employeeId = userDetails.getId();
        List<EmployeeExpenseResponseDto> employeeExpenses = employeeExpenseService.findAllExpenses(employeeId, travelPlanId);
        return ResponseEntity.ok(employeeExpenses);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{travel-plan-id}/add-expense")
    public ResponseEntity<Long> addEmployeeExpense(@PathVariable("travel-plan-id") Long travelPlanId, @RequestPart("file") MultipartFile file, @RequestPart("data") SubmitExpenseDto expenseDto, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long employeeId = userDetails.getId();
        return ResponseEntity.ok(employeeExpenseService.addExpense(expenseDto, employeeId, travelPlanId, file));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{travel-plan-id}/total")
    public ResponseEntity<BigDecimal> getTotalClaimAmount(@PathVariable("travel-plan-id") Long travelPlanId, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long employeeId = userDetails.getId();
        return ResponseEntity.ok(employeeExpenseService.getTotalClaimedAmount(travelPlanId, employeeId));
    }

    @GetMapping("/expense-proofs/{id}/view")
    public ResponseEntity<Resource> viewProof(@PathVariable Long id) throws IOException {

        FileResponseDto file = employeeExpenseService.getExpenseProofFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getResource());
    }
}
