package com.example.hrms.repositories;

import com.example.hrms.entities.JobReferral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobReferralRepository extends JpaRepository<JobReferral, Long> {
    List<JobReferral> findByJob_Id(Long jobId);
}
