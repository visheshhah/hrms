package com.example.hrms.repositories;

import com.example.hrms.dtos.travel.EmployeeWithTravelDto;
import com.example.hrms.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

//    @Query("SELECT e FROM Employee e WHERE e.user IS NULL")
//    List<Employee> findEmployeesWithoutUser();

    @Query("""
    SELECT e FROM Employee e
    WHERE e.id NOT IN (SELECT u.employee.id FROM User u)
    AND e.email <> 'system@hrms.com'
""")
    List<Employee> findAvailableEmployees();

//    @Query("""
//SELECT new com.example.hrms.dtos.travel.EmployeeWithTravelDto(
//    e.id,
//    e.name,
//    e.designation,
//    CASE
//        WHEN COUNT(et) > 0 THEN true
//        ELSE false
//    END
//)
//FROM Employee e
//LEFT JOIN EmployeeTravel et
//    ON et.employee.id = e.id
//    AND et.travelPlan.isActive = true
//    AND et.travelPlan.status = com.example.hrms.enums.TravelStatus.ACTIVE
//    AND et.travelPlan.startDate <= :slotDate
//    AND et.travelPlan.endDate >= :slotDate
//GROUP BY e.id, e.name, e.designation
//""")
//    List<EmployeeWithTravelDto> findEmployeesWithTravelStatus(LocalDate slotDate);
}
