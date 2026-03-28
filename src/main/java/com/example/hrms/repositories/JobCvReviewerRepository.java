package com.example.hrms.repositories;

import com.example.hrms.entities.Job;
import com.example.hrms.entities.JobCvReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface JobCvReviewerRepository extends JpaRepository<JobCvReviewer, Long> {

    @Modifying
    @Query("""
       DELETE FROM JobCvReviewer j
       WHERE j.job = :job
       AND j.reviewer.id IN :employeeIds
    """)
    void deleteByJobAndEmployeeIdIn(Job job, Set<Long> employeeIds);
}
