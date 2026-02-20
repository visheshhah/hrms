package com.example.hrms.services.job;

import com.example.hrms.dtos.job.JobReferralDto;
import com.example.hrms.dtos.job.JobReferralResponseDto;
import com.example.hrms.entities.*;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import com.example.hrms.services.files.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class JobReferralService {
    private final JobReferralRepository jobReferralRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ModelMapper modelMapper;
    private final EmployeeTravelRepository employeeTravelRepository;
    private final FileStorageService fileStorageService;
    private final DocumentTypeRepository documentTypeRepository;

    public JobReferralResponseDto saveReferral(JobReferralDto jobReferralDto, Long userId, Long jobId, MultipartFile file) {
        validateFilePresence(file);

        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee referredBy = creator.getEmployee();

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        DocumentType CVType = documentTypeRepository.findByCode("CV").orElseThrow(() -> new ResourceNotFoundException("CV document type missing"));

        validateFileFormat(file, CVType);

        JobReferral jobReferral = new JobReferral();
        jobReferral.setEmployee(referredBy);
        jobReferral.setDocumentType(CVType);

        if(!jobReferralDto.getComment().isEmpty()){
            jobReferral.setComment(jobReferralDto.getComment());
        }

        jobReferral.setJob(job);

        jobReferral.setFriendName(jobReferralDto.getFriendName());

        if(!jobReferralDto.getFriendEmail().isEmpty()){
            jobReferral.setFriendEmail(jobReferralDto.getFriendEmail());
        }

        String storedFileName = fileStorageService.store(file, "job-referral-cv");

        jobReferral.setFilePath(storedFileName);
        jobReferral.setFileName(file.getOriginalFilename());


        JobReferral savedJobReferral = jobReferralRepository.save(jobReferral);
        return modelMapper.map(savedJobReferral, JobReferralResponseDto.class);

    }

    private void validateFileFormat(MultipartFile file, DocumentType documentType) {
        String originalFilename = file.getOriginalFilename();

        if(originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file format");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();

        boolean allowed = Arrays.stream(documentType.getAllowedFormats().split(","))
                .map(String::trim)
                .anyMatch(s -> s.equalsIgnoreCase(extension));

        if(!allowed) {
            throw new IllegalArgumentException("Invalid file format");
        }

    }

    private void validateParticipant(Long travelPlanId, Long employeeId){
        boolean exists = employeeTravelRepository.existsByEmployeeIdAndTravelPlanId(employeeId, travelPlanId);
        if(!exists) {
            throw new AccessDeniedException("You are not assigned to this travel");
        }
    }

    private void validateFilePresence(MultipartFile file){
        if(file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Provided file is empty");
        }
    }
}
