package com.example.hrms.repositories;

import com.example.hrms.entities.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
    Optional<List<DocumentType>> findByIsActiveTrue();
    Optional<DocumentType> findByCode(String code);
}