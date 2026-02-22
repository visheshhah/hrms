package com.example.hrms.entities;

import com.example.hrms.enums.ExpenseCategory;
import com.example.hrms.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plain_id",  nullable = false)
    private TravelPlan travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id",  nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private ExpenseCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private CategoryType category;

    private String remark;

    @Column(nullable = false)
    private BigDecimal amount;

//    @Column(nullable = false)
//    private LocalDate expenseDate;

    private Instant submittedAt;
    private Instant decisionMadeAt;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "decision_by_employee_id")
    private Employee decisionByEmployee;

    @OneToMany(mappedBy = "expense", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExpenseProof> proofs;


}
