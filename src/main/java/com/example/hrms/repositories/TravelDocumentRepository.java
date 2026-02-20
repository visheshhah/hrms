package com.example.hrms.repositories;

import com.example.hrms.entities.TravelDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelDocumentRepository extends JpaRepository<TravelDocument, Long> {
}
