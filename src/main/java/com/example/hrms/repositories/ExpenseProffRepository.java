package com.example.hrms.repositories;

import com.example.hrms.entities.ExpenseProof;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseProffRepository extends JpaRepository<ExpenseProof, Long> {
}
