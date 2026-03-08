package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.GameSlot;
import com.example.hrms.entities.SlotRegistration;
import com.example.hrms.enums.SlotRegistrationStatus;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface SlotRegistrationRepository extends JpaRepository<SlotRegistration, Long> {

    SlotRegistration findByEmployeeAndSlot(Employee employee, GameSlot slot);

    boolean existsByEmployeeAndSlot(Employee employee, GameSlot slot);



    List<SlotRegistration> findBySlotAndStatus(
            GameSlot gameSlot,
            SlotRegistrationStatus status
    );

    @Query("""
   SELECT r FROM SlotRegistration r
   JOIN FETCH r.employee
   WHERE r.slot = :slot
   AND r.status = 'PENDING'
   ORDER BY r.slotCountAtRequest ASC, r.requestedAt ASC
""")
    List<SlotRegistration> findPendingRegistrationsOrdered(GameSlot slot);


    @Query("""
   SELECT r FROM SlotRegistration r
   WHERE r.slot = :slot
   AND r.status = 'PENDING'
   AND r.employee = :employee
""")
    SlotRegistration findBySlotAndEmployee(GameSlot slot, Employee employee);


//    @Query("""
//       SELECT r FROM SlotRegistration r
//       WHERE r.employee = :employee
//       AND r.status = 'PENDING'
//       And r.requestedAt > :yesterday
//    """)
//    List<SlotRegistration> findByEmployeeAndRequestedAt(Employee employee, Instant yesterday);

    @Query("""
       SELECT r FROM SlotRegistration r
       JOIN FETCH r.slot s
       JOIN FETCH s.game g
       WHERE r.employee = :employee
       AND r.status = 'PENDING'
       And s.slotDate = :today
       AND g.id = :gameId
    """)
    List<SlotRegistration> findActiveRegistrartionForToday(Employee employee, LocalDate today, Long gameId);
}
