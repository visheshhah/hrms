package com.example.hrms.entities;

import com.example.hrms.enums.SlotRegistrationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
public class SlotRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "game_slot_id", nullable = false)
    private GameSlot slot;

    @Column(nullable = false)
    private Integer slotCountAtRequest;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotRegistrationStatus status = SlotRegistrationStatus.PENDING;
}
