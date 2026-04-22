package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class DocumentType extends BaseClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(unique = true, nullable = false)
    private String name;

    private Boolean isActive = true;

    private String allowedFormats = "PDF,JPG,PNG";

    @ManyToOne
    @JoinColumn(name = "deleted_by_id")
    private Employee deletedBy;

    private Instant deletedAt;
}
