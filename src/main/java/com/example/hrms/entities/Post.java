package com.example.hrms.entities;

import com.example.hrms.enums.CelebrationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comments;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Post extends BaseClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private Employee createdBy;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private Boolean isSystemGenerated = Boolean.FALSE;

    @ManyToOne
    @JoinColumn(name = "deleted_by_id")
    private Employee deletedBy;

    private Boolean isDeleted = Boolean.FALSE;

    private Instant deletedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostTags> postTags = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade =  CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post",  cascade =  CascadeType.ALL)
    private List<Like> likes = new ArrayList<>();

    //
    private LocalDate celebrationDate;

    @ManyToOne
    @JoinColumn(name = "celebration_employee_id")
    private Employee celebrationEmployee;

    @Enumerated(EnumType.STRING)
    private CelebrationType celebrationType;
    //
}
