package com.example.hrms.repositories;

import com.example.hrms.entities.Expense;
import com.example.hrms.enums.ExpenseStatus;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @EntityGraph(attributePaths = "proofs")
    List<Expense> findByTravelPlanIdAndEmployeeId(Long travelPlanId, Long employeeId);

    @Query("""
    SELECT e FROM Expense e
    WHERE e.travelPlan.id = :travelPlanId
    AND e.status = "SUBMITTED"
""")
    List<Expense> findByTravelPlanIdAndStatusSubmitted(Long travelPlanId);

    @Query("""
    SELECT e FROM Expense e
    WHERE e.travelPlan.id = :travelPlanId
    AND e.employee.id =:employeeId
    AND e.status = "SUBMITTED"
""")
    List<Expense> findByTravelPlanIdAndEmployeeIdAndStatusSubmitted(Long travelPlanId, Long employeeId);

    @Query("""
    SELECT e FROM Expense e
    WHERE e.travelPlan.id = :travelPlanId
    AND e.status = "APPROVED"
""")
    List<Expense> findApprovedExpenseByTravelPlanIdAndStatusApproved(Long travelPlanId);

    @Query("""
    SELECT e FROM Expense e
    WHERE e.travelPlan.id = :travelPlanId
    AND e.employee.id =:employeeId
    AND e.status = "APPROVED"
""")
    List<Expense> findApprovedExpenseByTravelPlanIdAndEmployeeIdAndStatusApproved(Long travelPlanId, Long employeeId);

    @Query("""
    SELECT e FROM Expense e
    WHERE e.travelPlan.id = :travelPlanId
    AND e.employee.id =:employeeId
    AND e.status = :status
""")
    List<Expense> findByTravelPlanIdAndEmployeeIdAndStatus(Long travelPlanId, Long employeeId, ExpenseStatus status);
}
