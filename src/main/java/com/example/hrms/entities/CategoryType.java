package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Expense> expenses;

    private Boolean isActive = true;

    private Instant deletedAt;

    @ManyToOne
    @JoinColumn(name = "deleted_by_id")
    private Employee deletedBy;
}
