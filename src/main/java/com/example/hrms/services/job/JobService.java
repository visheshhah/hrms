package com.example.hrms.services.job;

import com.example.hrms.dtos.file.FileResponseDto;
import com.example.hrms.dtos.job.CreateJobDto;
import com.example.hrms.dtos.job.JobResponseDto;
import com.example.hrms.dtos.job.ViewJobReferralDto;
import com.example.hrms.entities.*;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import com.example.hrms.services.files.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JobCvReviewerRepository jobCvReviewerRepository;
    private final JobReferralRepository jobReferralRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public JobResponseDto createJob(CreateJobDto createJobDto, Long userId) {
        Job job = new Job();

        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isHr = creator.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr){
            throw new AccessDeniedException("You are not allowed to perform this action");
        }

        Employee creatorEmployee = creator.getEmployee();

        job.setTitle(createJobDto.getTitle());
        job.setDescription(createJobDto.getDescription());
        job.setCompanyName(createJobDto.getCompanyName());
        job.setJobType(createJobDto.getJobType());
        job.setLocation(createJobDto.getLocation());
        job.setMaxExperience(createJobDto.getMaxExperience());
        job.setMinExperience(createJobDto.getMinExperience());
        job.setWorkPlaceType(createJobDto.getWorkPlaceType());
        job.setCreator(creatorEmployee);

        //
        if(createJobDto.getReviewerIds() == null || createJobDto.getReviewerIds().isEmpty()){
            throw new IllegalArgumentException("Please select at least one reviewer");
        }
        Set<Long> reviewerIds = new HashSet<>(createJobDto.getReviewerIds());
        Job savedJob =  jobRepository.save(job);
        //List<JobCvReviewer> jobCvReviewers = new ArrayList<>();
        for(Long reviewerId : reviewerIds){
            Employee reviewer = employeeRepository.findById(reviewerId).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
            JobCvReviewer jobCvReviewer = new JobCvReviewer();
            jobCvReviewer.setJob(savedJob);
            jobCvReviewer.setReviewer(reviewer);
            //jobCvReviewers.add(jobCvReviewer);
            jobCvReviewerRepository.save(jobCvReviewer);
        }
        //
        return modelMapper.map(job, JobResponseDto.class);
    }

    public JobResponseDto getJobById(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return modelMapper.map(job, JobResponseDto.class);
    }

    public List<JobResponseDto> getAllJobs() {
        List<Job> jobs = jobRepository.findByStatus("Open").orElseThrow(() -> new ResourceNotFoundException("No Open Job found"));
        return jobs
                .stream()
                .map(job -> modelMapper.map(job, JobResponseDto.class))
                .toList();
    }

    public void closeJob(Long id, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = user.getEmployee();

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr){
            throw new AccessDeniedException("You are not allowed to perform this action");
        }

        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setStatus("Closed");
        job.setClosedAt(Instant.now());
        job.setClosedBy(employee);
        jobRepository.save(job);
    }


    public FileResponseDto getCvFile(Long cvId) throws IOException {

        JobReferral cv = jobReferralRepository.findById(cvId)
                .orElseThrow(() -> new IllegalArgumentException("CV not found"));

        Resource resource = fileStorageService
                .load("job-referral-cv", cv.getFilePath());

        Path path = Paths.get("uploads/private/job-referral-cv",
                cv.getFilePath());

        String detectedType = Files.probeContentType(path);

        String contentType = (detectedType != null)
                ? detectedType
                : "application/octet-stream";

        return new FileResponseDto(
                resource,
                cv.getFileName(),
                contentType
        );
    }

    public List<ViewJobReferralDto> getReferralsByJobId(Long jobId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        List<JobReferral>  jobReferrals = jobReferralRepository.findByJob_Id(jobId);
        return jobReferrals.stream()
                .map(jobReferral -> {
                    ViewJobReferralDto dto = new ViewJobReferralDto();
                    dto.setId(jobReferral.getId());
                    dto.setEmployeeName(jobReferral.getEmployee().getFirstName() + " " + jobReferral.getEmployee().getLastName());
                    if(jobReferral.getComment() != null && !jobReferral.getComment().isEmpty()){
                        dto.setComment(jobReferral.getComment());
                    }
                    dto.setFriendName(jobReferral.getFriendName());
                    if(jobReferral.getFriendEmail() != null && !jobReferral.getFriendEmail().isEmpty()){
                        dto.setFriendEmail(jobReferral.getFriendEmail());
                    }
                    dto.setFileName(jobReferral.getFileName());
                    return dto;
                })
                .toList();


    }
}
