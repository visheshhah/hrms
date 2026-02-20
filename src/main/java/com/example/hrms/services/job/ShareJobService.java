package com.example.hrms.services.job;

import com.example.hrms.dtos.job.ShareJobDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Job;
import com.example.hrms.entities.ShareJob;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.JobRepository;
import com.example.hrms.repositories.ShareJobRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShareJobService {
    private final ShareJobRepository shareJobRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public Long save(Long jobId, Long userId, ShareJobDto shareJobDto) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        Employee employee = user.getEmployee();

        Job job = jobRepository.findById(jobId).orElseThrow(()-> new ResourceNotFoundException("Job not found"));

        ShareJob shareJob = new ShareJob();
        shareJob.setEmail(shareJobDto.getEmail());
        shareJob.setSharedBy(employee);
        shareJob.setJob(job);

        ShareJob savedShareJob = shareJobRepository.save(shareJob);
        return savedShareJob.getId();

    }
}
