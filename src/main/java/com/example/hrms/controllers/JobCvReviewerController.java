package com.example.hrms.controllers;

import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.services.job.JobCvReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviewers")
public class JobCvReviewerController {
    private final JobCvReviewerService jobCvReviewerService;

    @PreAuthorize("hasRole('HR')")
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getReviewers() {
        return ResponseEntity.ok(jobCvReviewerService.getAllReviewers());
    }
}
