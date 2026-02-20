package com.example.hrms.repositories;

import com.example.hrms.entities.JobReferral;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobReferralRepository extends JpaRepository<JobReferral, Long> {
}
