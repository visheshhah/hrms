package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String gameName;

    private Boolean isActive = Boolean.TRUE;

    @OneToOne(mappedBy = "game")
    private ConfigureGame configureGame;
}
