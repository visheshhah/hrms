package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.EmployeeTravel;
import com.example.hrms.entities.TravelPlan;
import com.example.hrms.enums.TravelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EmployeeTravelRepository extends JpaRepository<EmployeeTravel, Long> {
    boolean existsByEmployeeIdAndTravelPlanId(Long employeeId, Long travelPlanId);
    List<EmployeeTravel> findByEmployeeId(Long id);

    @Modifying
    @Query("""
       DELETE FROM EmployeeTravel e
       WHERE e.travelPlan = :travelPlan
       AND e.employee.id IN :employeeIds
    """)
    void deleteByTravelPlanAndEmployeeIdIn(TravelPlan travelPlan, Set<Long> employeeIds);


    @Query("""
        SELECT et FROM EmployeeTravel et
        JOIN FETCH et.travelPlan t
        WHERE et.employee.id = :id
        AND t.status = :status
"""
    )
    List<EmployeeTravel> findByEmployeeIdAndStatus(Long id, TravelStatus status);

    @Query("""
    SELECT e FROM EmployeeTravel e
    JOIN FETCH e.travelPlan tp
    WHERE e.employee = :employee
    AND (tp.status = com.example.hrms.enums.TravelStatus.DRAFT
    OR tp.status = com.example.hrms.enums.TravelStatus.ACTIVE)
    AND tp.isActive = true
"""
    )
    List<EmployeeTravel> findExistingTravels(Employee employee);

    @Query("""
    SELECT COUNT(et) > 0
    FROM EmployeeTravel et
    WHERE et.employee.id = :employeeId
      AND et.travelPlan.status = com.example.hrms.enums.TravelStatus.ACTIVE
      AND et.travelPlan.startDate <= :slotDate
      AND et.travelPlan.endDate >= :slotDate
""")
    boolean existsTravelConflict(Long employeeId, LocalDate slotDate);
}

