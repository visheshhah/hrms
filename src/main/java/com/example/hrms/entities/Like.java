package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames =
                        {"post_id", "liked_by_id"})
        }
)
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liked_by_id", nullable = false)
    private Employee likedBy;

    private Instant likedAt = Instant.now();
}
