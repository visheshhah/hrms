package com.example.hrms.repositories;

import com.example.hrms.entities.EmployeeTravel;
import com.example.hrms.entities.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeTravelRepository extends JpaRepository<EmployeeTravel, Long> {
    boolean existsByEmployeeIdAndTravelPlanId(Long employeeId, Long travelPlanId);
    Optional<List<TravelPlan>> findByEmployeeId(Long id);

}
