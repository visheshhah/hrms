package com.example.hrms.repositories;

import com.example.hrms.entities.Expense;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @EntityGraph(attributePaths = "proofs")
    List<Expense> findByTravelPlanIdAndEmployeeId(Long travelPlanId, Long employeeId);
}
