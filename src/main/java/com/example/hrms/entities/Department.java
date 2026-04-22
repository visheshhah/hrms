package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String departmentName;

    private Boolean isActive = Boolean.TRUE;

    @OneToMany(mappedBy = "department")
    private List<Employee> employee;
}
