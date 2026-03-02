package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"employee_id", "game_id"}
        )
)
public class EmployeeGameInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id",  nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "game_id",  nullable = false)
    private Game game;
}
