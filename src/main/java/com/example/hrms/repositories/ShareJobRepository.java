package com.example.hrms.repositories;

import com.example.hrms.entities.ShareJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareJobRepository extends JpaRepository<ShareJob, Long> {
}
