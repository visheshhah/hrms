package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findEmployeesByManagerId(Long managerId);
}
