package com.example.hrms.controllers;

import com.example.hrms.dtos.expense.SubmitExpenseDto;
import com.example.hrms.dtos.job.JobReferralDto;
import com.example.hrms.dtos.job.JobReferralResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.repositories.JobReferralRepository;
import com.example.hrms.services.job.JobReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobReferralController {
    private final JobReferralService jobReferralService;


    @PostMapping(value = "/{job-id}/refer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobReferralResponseDto> giveReferral(@PathVariable("job-id") Long jobId, @RequestPart("file") MultipartFile file, @RequestPart("data") JobReferralDto referralDto, @AuthenticationPrincipal MyUserDetails userDetails){
        Long employeeId = userDetails.getId();
        return ResponseEntity.ok(jobReferralService.saveReferral(referralDto, employeeId, jobId, file));
    }

}
