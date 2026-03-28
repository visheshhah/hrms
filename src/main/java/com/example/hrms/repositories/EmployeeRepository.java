package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<List<Employee>> findEmployeesByManagerId(Long managerId);

    @Query("""
       SELECT e FROM Employee e
       WHERE FUNCTION('MONTH', e.joiningDate) = :month
       AND FUNCTION('DAY', e.joiningDate) = :day
       """)
    List<Employee> findEmployeesWithAnniversary(int month, int day);

    @Query("""
       SELECT e FROM Employee e
       WHERE FUNCTION('MONTH', e.dateOfBirth) = :month
       AND FUNCTION('DAY', e.dateOfBirth) = :day
       """)
    List<Employee> findEmployeesWithBirthday(int month, int day);

    Optional<Employee> findByEmail(String email);

    @Query("""
       SELECT e FROM Employee e
       WHERE e.id <> 10
       AND e.isDeleted = false
    """)
    List<Employee> findAllEmployees();
}
