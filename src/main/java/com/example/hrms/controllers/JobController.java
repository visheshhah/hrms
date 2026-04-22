package com.example.hrms.controllers;

import com.example.hrms.dtos.file.FileResponseDto;
import com.example.hrms.dtos.job.*;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.job.JobService;
import com.example.hrms.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobResponseDto>> getAllJobs(@RequestHeader("Authorization") String token) {
        List<JobResponseDto> jobResponseDto = jobService.getAllJobs();
        return new ResponseEntity<>(jobResponseDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/create")
    public ResponseEntity<JobResponseDto> createJob(@RequestPart("file") MultipartFile file, @RequestPart("data") CreateJobDto createJobDto, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long userId = userDetails.getId();
        JobResponseDto jobResponseDto = jobService.createJob(createJobDto, userId, file);
        return ResponseEntity.ok(jobResponseDto);
    }

//    @PreAuthorize("hasRole('HR')")
//    @PatchMapping("/update/{jobId}")
//    public ResponseEntity<JobResponseDto> updateJob(@PathVariable("jobId") Long jobId, @RequestBody UpdateJobDto updateJobDto, @AuthenticationPrincipal MyUserDetails userDetails) {
//        JobResponseDto jobResponseDto = jobService.updateJob(jobId, updateJobDto, userDetails.getId());
//        return ResponseEntity.ok(jobResponseDto);
//    }


    @GetMapping("/{Id}")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable("Id") Long id) {
        JobResponseDto jobResponseDto = jobService.getJobById(id);
        return ResponseEntity.ok(jobResponseDto);
    }

    @PreAuthorize("hasRole('HR')")
    @DeleteMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> deleteJobById(@PathVariable("jobId") Long jobId, @AuthenticationPrincipal MyUserDetails userDetails) {
        jobService.closeJob(jobId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/referral/{jobId}")
    public ResponseEntity<List<ViewJobReferralDto>> viewJobReferral(@PathVariable("jobId") Long jobId, @AuthenticationPrincipal MyUserDetails userDetails){
        return ResponseEntity.ok(jobService.getReferralsByJobId(jobId, userDetails.getId()));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/cv/{id}/view")
    public ResponseEntity<Resource> viewCv(@PathVariable Long id) throws IOException {

        FileResponseDto file = jobService.getCvFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getResource());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{Id}/detail")
    public ResponseEntity<JobDetailDto> getDetailJobById(@PathVariable("Id") Long id) {
        JobDetailDto jobResponseDto = jobService.getJobDetailById(id);
        return ResponseEntity.ok(jobResponseDto);
    }

    @GetMapping("/job-descriptions/{id}/view")
    public ResponseEntity<Resource> viewProof(@PathVariable Long id) throws IOException {

        FileResponseDto file = jobService.getJobDescriptionFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getResource());
    }

    @GetMapping("/{Id}/job-detail")
    public ResponseEntity<JobDetailWithDocDto> getJobWithJdById(@PathVariable("Id") Long id) {
        JobDetailWithDocDto jobResponseDto = jobService.getJobWithDocById(id);
        return ResponseEntity.ok(jobResponseDto);
    }

    @PreAuthorize("hasRole('HR')")
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobResponseDto> updateJob(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("data") UpdateJobDto updateJobDto,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        Long userId = userDetails.getId();

        JobResponseDto response = jobService.updateJobWithJd(id, updateJobDto, userId, file);

        return ResponseEntity.ok(response);
    }
}
