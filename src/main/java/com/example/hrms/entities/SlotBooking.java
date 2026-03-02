package com.example.hrms.entities;

import com.example.hrms.enums.SlotBookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"game_slot_id", "employee_id"}
        )
)
public class SlotBooking extends BaseClass{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_slot_id", nullable = false)
    private GameSlot gameSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotBookingStatus status = SlotBookingStatus.CONFIRMED;
}
