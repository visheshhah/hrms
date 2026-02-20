package com.example.hrms.services.job;

import com.example.hrms.dtos.job.CreateJobDto;
import com.example.hrms.dtos.job.JobResponseDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Job;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.JobRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public JobResponseDto createJob(CreateJobDto createJobDto, Long userId) {
        Job job = new Job();

        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
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


        job =  jobRepository.save(job);
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
                .collect(Collectors.toList());
    }

    public void closeJob(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setStatus("Closed");
        jobRepository.save(job);
    }




}
