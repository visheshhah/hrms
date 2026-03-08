package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
    SELECT n FROM Notification n
    JOIN FETCH n.sender s
    WHERE n.receiver = :employee
    AND n.readStatus = false
    AND n.notificationDate = :date
""")
    List<Notification> findByEmployeeAndDate(Employee employee, LocalDate date);


    @Query("""
    SELECT n FROM Notification n
    JOIN FETCH n.sender s
    WHERE n.receiver = :employee
""")
    List<Notification> findAllNotificationByEmployee(Employee employee);
}
