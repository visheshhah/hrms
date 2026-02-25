package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tagName;

    private Boolean isActive = Boolean.TRUE;

    @OneToMany(mappedBy = "tag")
    private List<PostTags> postTags;

}
