package com.example.hrms.entities;

import com.example.hrms.enums.ERole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, unique = true)
    private ERole name;
}
