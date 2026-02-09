package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "employee_travels")
public class EmployeeTravel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;

    @Column(nullable = false)
    private Instant assignedAt =  Instant.now();
}
