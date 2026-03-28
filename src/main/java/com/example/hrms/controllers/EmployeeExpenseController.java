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
import java.nio.file.AccessDeniedException;
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

    @GetMapping("/expense-proofs/{id}/view")
    public ResponseEntity<Resource> viewProof(@PathVariable Long id) throws IOException {

        FileResponseDto file = employeeExpenseService.getExpenseProofFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getResource());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{travel-plan-id}/claim/total")
    public ResponseEntity<BigDecimal> getTotalClaimedAmountByTravelPlanAndEmployee(@PathVariable("travel-plan-id") Long travelPlanId, @AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {
        return ResponseEntity.ok(employeeExpenseService.getTotalClaimedAmountByTravelPlanAndEmployee(travelPlanId, userDetails.getId()));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{travel-plan-id}/approved/total")
    public ResponseEntity<BigDecimal> getTotalApprovedAmountByTravelPlanAndEmployee(@PathVariable("travel-plan-id") Long travelPlanId, @AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {
        return ResponseEntity.ok(employeeExpenseService.getTotalApprovedAmountByTravelPlanAndEmployee(travelPlanId, userDetails.getId()));
    }

    @GetMapping("/{travel-plan-id}/{employee-id}/filter")
    public ResponseEntity<List<EmployeeExpenseResponseDto>> getEmployeeExpenseByStatus(@PathVariable("travel-plan-id") Long travelPlanId, @PathVariable("employee-id")  Long employeeId, @RequestParam(defaultValue = "submitted") String status) {
        List<EmployeeExpenseResponseDto> employeeExpenses = employeeExpenseService.findAllExpenseByStatus(employeeId, travelPlanId, status);
        return ResponseEntity.ok(employeeExpenses);
    }
}
