package com.example.hrms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class ConfigureGame extends BaseClass{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    @Min(1)
    private Integer maxPlayers;

    @Column(nullable = false)
    @Min(1)
    private Integer slotDuration;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private Employee createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private Employee updatedBy;

    @OneToOne
    @JoinColumn(name = "game_id", nullable = false, unique = true)
    private Game game;

    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "deleted_by_id")
    private Employee deletedBy;

    private Instant deletedAt;

}
