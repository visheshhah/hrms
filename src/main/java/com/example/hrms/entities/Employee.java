package com.example.hrms.entities;

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
public class Employee{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private LocalDate joiningDate;
    private LocalDate dateOfBirth;
    private BigDecimal salary;
    private String designation;

    @OneToOne(mappedBy = "employee")
    private User user;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "manager")
    private List<Employee> employees;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "deleted_by_id")
    private Employee deletedBy;

    private Boolean isDeleted = Boolean.FALSE;

    private Instant deletedAt;
}
