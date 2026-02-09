package com.example.hrms.entities;

import com.example.hrms.enums.ExpenseCategory;
import com.example.hrms.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plain_id",  nullable = false)
    private TravelPlan travelPlan;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    private String remark;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    private Instant decisionMadeAt;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "decision_by_employee_id")
    private Long decisionByEmployee;




}
