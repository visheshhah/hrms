package com.example.hrms.controllers;

import com.example.hrms.dtos.job.ShareJobDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.job.ShareJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/share/job")
public class ShareJobController {
    private final ShareJobService shareJobService;

    @PostMapping("/{jobId}")
    public ResponseEntity<Long> shareJob(@PathVariable("jobId") Long jobId, @AuthenticationPrincipal MyUserDetails userDetails, ShareJobDto shareJobDto){
        Long sharedById = userDetails.getId();
        return ResponseEntity.ok(shareJobService.save(jobId, sharedById, shareJobDto));
    }

}
