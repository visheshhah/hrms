package com.example.hrms.repositories;

import com.example.hrms.entities.Expense;
import com.example.hrms.entities.TravelDocument;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelDocumentRepository extends JpaRepository<TravelDocument, Long> {

    List<TravelDocument> findByTravelPlanIdAndEmployeeId(Long travelPlanId, Long employeeId);
}
