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

    Optional<DocumentType> findByIdAndIsActiveTrue(Long id);

    List<DocumentType> findAllByIsActiveTrue();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    List<DocumentType> findByDeletedAtIsNull();

    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Optional<DocumentType> findByIdAndDeletedAtIsNull(Long id);

    Optional<DocumentType> findByCodeIgnoreCase(String code);
    Optional<DocumentType> findByNameIgnoreCase(String name);
}