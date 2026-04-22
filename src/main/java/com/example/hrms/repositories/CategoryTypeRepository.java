package com.example.hrms.repositories;

import com.example.hrms.entities.CategoryType;
import io.micrometer.common.KeyValues;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryTypeRepository extends JpaRepository<CategoryType, Long> {
    boolean existsByName(String name);

    Optional<CategoryType> findByIdAndIsActiveTrue(Long id);

    boolean existsByNameIgnoreCase(String newName);
    Optional<CategoryType> findByNameIgnoreCase(String name);

    List<CategoryType> findByIsActiveTrue();}
