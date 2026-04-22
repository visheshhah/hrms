package com.example.hrms.services.job;

import com.example.hrms.dtos.file.FileResponseDto;
import com.example.hrms.dtos.job.*;
import com.example.hrms.entities.*;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import com.example.hrms.services.files.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class JobService {
    private final JobRepository jobRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JobCvReviewerRepository jobCvReviewerRepository;
    private final JobReferralRepository jobReferralRepository;
    private final FileStorageService fileStorageService;
    private final DocumentTypeRepository documentTypeRepository;
    private final JobDocumentRepository jobDocumentRepository;

    @Transactional
    public JobResponseDto createJob(CreateJobDto createJobDto, Long userId, MultipartFile file) {
        Job job = new Job();
        validateFilePresence(file);
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
        DocumentType jdType = documentTypeRepository.findByCode("JD").orElseThrow(() -> new ResourceNotFoundException("Job description document type missing"));
        validateFileFormat(file, jdType);
        String storedFileName = fileStorageService.store(file, "job-descriptions");

        JobDocument jobDocument = new JobDocument();
        jobDocument.setJob(savedJob);
        jobDocument.setFileName(file.getOriginalFilename());
        jobDocument.setFilePath(storedFileName);
        jobDocument.setDocumentType(jdType);
        jobDocument.setUploadedBy(creatorEmployee);
        jobDocumentRepository.save(jobDocument);
        return modelMapper.map(job, JobResponseDto.class);
    }

    public FileResponseDto getJobDescriptionFile(Long jdId) throws IOException {

        JobDocument jd = jobDocumentRepository.findById(jdId)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));

        Resource resource = fileStorageService
                .load("job-descriptions", jd.getFilePath());

        Path path = Paths.get("uploads/private/job-descriptions",
                jd.getFilePath());

        String detectedType = Files.probeContentType(path);

        String contentType = (detectedType != null)
                ? detectedType
                : "application/octet-stream";

        return new FileResponseDto(
                resource,
                jd.getFileName(),
                contentType
        );
    }

    public JobResponseDto updateJob(Long jobId, UpdateJobDto updateJobDto, Long userId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(updateJobDto.getTitle() != null && !updateJobDto.getTitle().isBlank()){
            job.setTitle(updateJobDto.getTitle());
        }

        if(updateJobDto.getDescription() != null && !updateJobDto.getDescription().isBlank()){
            job.setDescription(updateJobDto.getDescription());
        }
        if(updateJobDto.getCompanyName() != null && !updateJobDto.getCompanyName().isBlank()){
            job.setCompanyName(updateJobDto.getCompanyName());
        }
        if(updateJobDto.getJobType() != null && !updateJobDto.getJobType().isBlank()){
            job.setJobType(updateJobDto.getJobType());
        }
        if(updateJobDto.getLocation() != null && !updateJobDto.getLocation().isBlank()){
            job.setLocation(updateJobDto.getLocation());
        }
        if(updateJobDto.getWorkPlaceType() != null && !updateJobDto.getWorkPlaceType().isBlank()){
            job.setWorkPlaceType(updateJobDto.getWorkPlaceType());
        }
        if((updateJobDto.getMinExperience() != null || !updateJobDto.getMinExperience().isNaN())
            && (updateJobDto.getMaxExperience() != null || !updateJobDto.getMaxExperience().isNaN())){
            if(updateJobDto.getMaxExperience() < updateJobDto.getMinExperience()){
                throw new IllegalArgumentException("Max Experience cannot be less than Min Experience");
            }
            job.setMaxExperience(updateJobDto.getMaxExperience());
            job.setMinExperience(updateJobDto.getMinExperience());
        }
        if(updateJobDto.getMinExperience() != null || !updateJobDto.getMinExperience().isNaN()){
            if(updateJobDto.getMaxExperience() < updateJobDto.getMinExperience()){
                throw new IllegalArgumentException("Min Experience cannot be less than Max Experience");
            }
            job.setMinExperience(updateJobDto.getMinExperience());
        }
        if(updateJobDto.getMaxExperience() != null || !updateJobDto.getMaxExperience().isNaN()){
            if(updateJobDto.getMaxExperience() < updateJobDto.getMinExperience()){
                throw new IllegalArgumentException("Max Experience cannot be less than Min Experience");
            }
            job.setMaxExperience(updateJobDto.getMaxExperience());
        }

        List<JobCvReviewer> existingCvReviewers = job.getJobCvReviewers();

        Set<Long> existingCvReviewersIds = existingCvReviewers.stream().map(cvReviewer -> cvReviewer.getReviewer().getId()).collect(Collectors.toSet());
        Set<Long> newCvReviewersIds = updateJobDto.getJobCvReviewerIds() == null
                ? Set.of()
                : new HashSet<>(updateJobDto.getJobCvReviewerIds());

        Set<Long> toAdd = new HashSet<>(newCvReviewersIds);
        toAdd.removeAll(existingCvReviewersIds);

        Set<Long> toRemove = new HashSet<>(existingCvReviewersIds);
        toRemove.removeAll(newCvReviewersIds);

        if (!toRemove.isEmpty()) {
            jobCvReviewerRepository.deleteByJobAndEmployeeIdIn(job, toRemove);
        }

        if (!toAdd.isEmpty()) {
            List<Employee> employeesToAdd = employeeRepository.findAllById(toAdd);

            for (Employee employee : employeesToAdd) {
                JobCvReviewer jobCvReviewer = new JobCvReviewer();
                jobCvReviewer.setReviewer(employee);
                jobCvReviewer.setJob(job);
                jobCvReviewerRepository.save(jobCvReviewer);
            }
        }

        Job updatedJob = jobRepository.save(job);
        return modelMapper.map(updatedJob, JobResponseDto.class);

    }

    public JobResponseDto getJobById(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return modelMapper.map(job, JobResponseDto.class);
    }

    public JobDetailWithDocDto getJobWithDocById(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        JobDetailWithDocDto jobDetailWithDocDto = new JobDetailWithDocDto();
        jobDetailWithDocDto.setId(job.getId());
        jobDetailWithDocDto.setTitle(job.getTitle());
        jobDetailWithDocDto.setDescription(job.getDescription());
        jobDetailWithDocDto.setLocation(job.getLocation());
        jobDetailWithDocDto.setWorkPlaceType(job.getWorkPlaceType());
        jobDetailWithDocDto.setMinExperience(job.getMinExperience());
        jobDetailWithDocDto.setMaxExperience(job.getMaxExperience());
        jobDetailWithDocDto.setJobType(job.getJobType());
        jobDetailWithDocDto.setCompanyName(job.getCompanyName());
        jobDetailWithDocDto.setStatus(job.getStatus());
        List<JobDocument> documents = job.getDocuments();

        if (documents != null && !documents.isEmpty()) {
            JobDocument doc = documents.get(0);

            jobDetailWithDocDto.setJdId(doc.getId());
            jobDetailWithDocDto.setFileName(doc.getFileName());
        }
        return jobDetailWithDocDto;
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

    public JobDetailDto getJobDetailById(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        JobDetailDto jobDetailDto =new JobDetailDto();
        jobDetailDto.setId(job.getId());
        jobDetailDto.setTitle(job.getTitle());
        jobDetailDto.setDescription(job.getDescription());
        jobDetailDto.setCompanyName(job.getCompanyName());
        jobDetailDto.setLocation(job.getLocation());
        jobDetailDto.setWorkPlaceType(job.getWorkPlaceType());
        jobDetailDto.setJobType(job.getJobType());
        jobDetailDto.setMinExperience(job.getMinExperience());
        jobDetailDto.setMaxExperience(job.getMaxExperience());
        Set<Long> reviewrIds = job.getJobCvReviewers().stream()
                .map(j -> j.getReviewer().getId())
                        .collect(Collectors.toSet());
        jobDetailDto.setReviewerIds(reviewrIds);
        List<JobDocument> documents = job.getDocuments();
        if (documents != null && !documents.isEmpty()) {
            JobDocument doc = documents.get(0);

            jobDetailDto.setJdId(doc.getId());
            jobDetailDto.setFileName(doc.getFileName());
        }
        return jobDetailDto;
    }

    private void validateFilePresence(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Provided file is empty");
        }
    }

    private void validateFileFormat(MultipartFile file, DocumentType documentType) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file format");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();

        boolean allowed = Arrays.stream(documentType.getAllowedFormats().split(","))
                .map(String::trim)
                .anyMatch(s -> s.equalsIgnoreCase(extension));

        if (!allowed) {
            throw new IllegalArgumentException("Invalid file format");
        }

    }

    @Transactional
    public void updateJobDocument(Long documentId, MultipartFile file, Employee employee) {

        validateFilePresence(file);

        DocumentType jdType = documentTypeRepository.findByCode("JD")
                .orElseThrow(() -> new ResourceNotFoundException("Job description document type missing"));

        validateFileFormat(file, jdType);

        JobDocument doc = jobDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        String oldFilePath = doc.getFilePath();

        String newFileName = fileStorageService.store(file, "job-descriptions");

        try {
//            String originalName = Optional.ofNullable(file.getOriginalFilename())
//                    .orElse("unknown");

            doc.setFileName(file.getOriginalFilename());
            doc.setFilePath(newFileName);
            doc.setUploadedAt(Instant.now());
            doc.setDocumentType(jdType);
            doc.setUploadedBy(employee);
            jobDocumentRepository.save(doc);

        } catch (Exception e) {
            fileStorageService.delete("job-descriptions", newFileName);
            throw e;
        }

        if (oldFilePath != null) {
            try {
                fileStorageService.delete("job-descriptions", oldFilePath);
            } catch (Exception ex) {
                log.warn("Failed to delete old file {}", oldFilePath, ex);
            }
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public JobResponseDto updateJobWithJd(Long jobId, UpdateJobDto updateJobDto, Long userId, MultipartFile jdFile) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobDocument jd = jobDocumentRepository.findByJobId(jobId).orElse(null);
        if (jdFile != null && !jdFile.isEmpty()) {

            if (jd == null) {
                JobDocument newDoc = new JobDocument();
                DocumentType jdType = documentTypeRepository.findByCode("JD")
                        .orElseThrow(() -> new ResourceNotFoundException("Job description document type missing"));

                validateFileFormat(jdFile, jdType);

                String fileName = fileStorageService.store(jdFile, "job-descriptions");

                newDoc.setJob(job);
                newDoc.setFileName(jdFile.getOriginalFilename());
                newDoc.setFilePath(fileName);
                newDoc.setUploadedAt(Instant.now());
                newDoc.setDocumentType(jdType);
                newDoc.setUploadedBy(user.getEmployee());
                jobDocumentRepository.save(newDoc);

            } else {

                updateJobDocument(jd.getId(), jdFile, user.getEmployee());
            }
        }
        if(updateJobDto.getTitle() != null && !updateJobDto.getTitle().isBlank()){
            job.setTitle(updateJobDto.getTitle());
        }

        if(updateJobDto.getDescription() != null && !updateJobDto.getDescription().isBlank()){
            job.setDescription(updateJobDto.getDescription());
        }
        if(updateJobDto.getCompanyName() != null && !updateJobDto.getCompanyName().isBlank()){
            job.setCompanyName(updateJobDto.getCompanyName());
        }
        if(updateJobDto.getJobType() != null && !updateJobDto.getJobType().isBlank()){
            job.setJobType(updateJobDto.getJobType());
        }
        if(updateJobDto.getLocation() != null && !updateJobDto.getLocation().isBlank()){
            job.setLocation(updateJobDto.getLocation());
        }
        if(updateJobDto.getWorkPlaceType() != null && !updateJobDto.getWorkPlaceType().isBlank()){
            job.setWorkPlaceType(updateJobDto.getWorkPlaceType());
        }
        Double minExp = updateJobDto.getMinExperience();
        Double maxExp = updateJobDto.getMaxExperience();

        if (minExp != null && maxExp != null) {
            if (maxExp < minExp) {
                throw new IllegalArgumentException("Max Experience cannot be less than Min Experience");
            }
            job.setMinExperience(minExp);
            job.setMaxExperience(maxExp);
        } else if (minExp != null) {
            job.setMinExperience(minExp);
        } else if (maxExp != null) {
            job.setMaxExperience(maxExp);
        }

        List<JobCvReviewer> existingCvReviewers = job.getJobCvReviewers();

        Set<Long> existingCvReviewersIds = existingCvReviewers.stream().map(cvReviewer -> cvReviewer.getReviewer().getId()).collect(Collectors.toSet());
        Set<Long> newCvReviewersIds = updateJobDto.getJobCvReviewerIds() == null
                ? Set.of()
                : new HashSet<>(updateJobDto.getJobCvReviewerIds());

        Set<Long> toAdd = new HashSet<>(newCvReviewersIds);
        toAdd.removeAll(existingCvReviewersIds);

        Set<Long> toRemove = new HashSet<>(existingCvReviewersIds);
        toRemove.removeAll(newCvReviewersIds);

        if (!toRemove.isEmpty()) {
            jobCvReviewerRepository.deleteByJobAndEmployeeIdIn(job, toRemove);
        }

        if (!toAdd.isEmpty()) {
            List<Employee> employeesToAdd = employeeRepository.findAllById(toAdd);

            for (Employee employee : employeesToAdd) {
                JobCvReviewer jobCvReviewer = new JobCvReviewer();
                jobCvReviewer.setReviewer(employee);
                jobCvReviewer.setJob(job);
                jobCvReviewerRepository.save(jobCvReviewer);
            }
        }

        Job updatedJob = jobRepository.save(job);
        return modelMapper.map(updatedJob, JobResponseDto.class);

    }
}
