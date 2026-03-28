package com.example.hrms.repositories;

import com.example.hrms.entities.TravelPlan;
import com.example.hrms.enums.TravelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
//    Optional<List<TravelPlan>> findByEmployeeId(Long id);

    @Query("""
        SELECT tp FROM TravelPlan tp
        WHERE tp.isActive = true
""")
    List<TravelPlan> findTravels();

    @Query("""
        SELECT tp FROM TravelPlan tp
        WHERE tp.isActive = true
        AND tp.status = :status
""")
    List<TravelPlan> findTravelsByStatus(TravelStatus status);
}
