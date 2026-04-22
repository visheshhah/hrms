package com.example.hrms.repositories;

import com.example.hrms.entities.JobDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobDocumentRepository extends JpaRepository<JobDocument, Long> {
    Optional<JobDocument> findByJobId(Long jobId);
}
