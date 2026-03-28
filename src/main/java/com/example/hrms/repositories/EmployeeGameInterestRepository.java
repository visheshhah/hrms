package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.EmployeeGameInterest;
import com.example.hrms.entities.Game;
import io.micrometer.common.KeyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface EmployeeGameInterestRepository extends JpaRepository<EmployeeGameInterest, Long> {
    boolean existsByEmployeeAndGame(Employee employee, Game game);

    void deleteByEmployee(Employee employee);

    List<EmployeeGameInterest> findByEmployee(Employee employee);

    @Modifying
    @Query("""
   DELETE FROM EmployeeGameInterest e
   WHERE e.employee = :employee
   AND e.game.id IN :gameIds
""")
    void deleteByEmployeeAndGameIdIn(Employee employee, Set<Long> gameIds);

//    @Query("""
//    SELECT eg FROM EmployeeGameInterest eg
//    WHERE eg.game = :game
//""")
    List<EmployeeGameInterest> findByGame(Game game);
}
