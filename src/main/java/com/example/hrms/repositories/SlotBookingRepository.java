package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.GameSlot;
import com.example.hrms.entities.SlotBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SlotBookingRepository extends JpaRepository<SlotBooking, Long> {

    boolean existsByGameSlotAndEmployee(GameSlot slot, Employee employee);


    SlotBooking findByGameSlotAndEmployee(GameSlot slot, Employee employee);

//    @Query("""
//    SELECT b FROM SlotBooking b
//    JOIN FETCH b.gameSlot g
//    JOIN FETCH g.game gt
//    WHERE b.employee = :employee
//    AND b.status = 'CONFIRMED'
//    AND g.slotDate = :today
//    AND g.startTime > :now
//
//""")
//    List<SlotBooking> findByEmployeeAndDateAndTime(
//            @Param("employee") Employee employee,
//            @Param("today") LocalDate today,
//            @Param("now") LocalTime now
//    );

    @Query("""
    SELECT b FROM SlotBooking b
    JOIN FETCH b.gameSlot g
    JOIN FETCH g.game gt
    WHERE b.employee = :employee
    AND b.status = 'CANCELLED'
""")
    List<SlotBooking> findByEmployeeAndStatusIsCancelled(Employee employee);

//    @Query("""
//    SELECT b FROM SlotBooking b
//    JOIN FETCH b.gameSlot g
//    JOIN FETCH g.game gt
//    WHERE b.employee = :employee
//    AND b.status = 'CONFIRMED'
//    AND (
//        g.slotDate < :today
//        OR (g.slotDate = :today AND g.endTime < :nowTime)
//    )
//""")
//    List<SlotBooking> findByEmployeeAndStatusIsConfirmed(Employee employee, LocalDate today, LocalTime nowTime);

    @Query("""
    SELECT b FROM SlotBooking b
    JOIN FETCH b.gameSlot g
    JOIN FETCH g.game gt
    WHERE b.employee = :employee
    AND b.status = 'CONFIRMED'
""")
    List<SlotBooking> findByEmployeeAndStatusIsConfirmed(Employee employee);


    @Query("""
    SELECT b FROM SlotBooking b
    JOIN FETCH b.gameSlot g
    WHERE b.employee = :employee
    AND b.status = 'CONFIRMED'
    AND g.slotDate = :date
""")
    List<SlotBooking> findByEmployeeAndDate(Employee employee, LocalDate date);

}
