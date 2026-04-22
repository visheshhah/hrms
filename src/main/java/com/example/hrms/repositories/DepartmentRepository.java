package com.example.hrms.repositories;

import com.example.hrms.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByIsActiveTrue();

    Optional<Department> findByIdAndIsActiveTrue(Long id);

    Optional<Department> findByDepartmentName(String departmentName);
}
