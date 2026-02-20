package com.example.hrms.repositories;

import com.example.hrms.entities.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
//    Optional<List<TravelPlan>> findByEmployeeId(Long id);
}
