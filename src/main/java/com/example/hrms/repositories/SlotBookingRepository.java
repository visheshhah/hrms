package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.GameSlot;
import com.example.hrms.entities.SlotBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotBookingRepository extends JpaRepository<SlotBooking, Long> {

    boolean existsByGameSlotAndEmployee(GameSlot slot, Employee employee);


    SlotBooking findByGameSlotAndEmployee(GameSlot slot, Employee employee);
}
