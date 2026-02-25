package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Comment extends BaseClass{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Employee author;

    private String commentText;

    private Boolean isEdited = Boolean.FALSE;

    @ManyToOne
    @JoinColumn(name = "deleted_by_id")
    private Employee deletedBy;

    private Boolean isDeleted = Boolean.FALSE;

    private Instant deletedAt;
}
