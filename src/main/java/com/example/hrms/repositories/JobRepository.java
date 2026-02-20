package com.example.hrms.repositories;

import com.example.hrms.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<List<Job>> findByStatus(String status);
}
