package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Game;
import com.example.hrms.entities.WeeklySlotCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WeeklySlotCountRepository extends JpaRepository<WeeklySlotCount, Long> {
    WeeklySlotCount findByEmployee_IdAndWeekStartDateAndGame_Id(Long employeeId, LocalDate weekStartDate,  Long gameId);

    WeeklySlotCount findByEmployeeAndGameAndWeekStartDate(
            Employee employee,
            Game game,
            LocalDate weekStartDate
    );
}
