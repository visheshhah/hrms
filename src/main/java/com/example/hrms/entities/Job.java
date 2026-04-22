package com.example.hrms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    @Min(value = 0)
    private Double minExperience;

    @Column(nullable = false)
    @Min(value = 0)
    private Double maxExperience;

    @Column(nullable = false)
    private String jobType;

    @Column(nullable = false)
    private String workPlaceType;

    private String status = "Open";

    @ManyToOne
    @JoinColumn(name = "closed_by_id")
    private Employee closedBy;

    private Instant closedAt;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private Employee creator;

    //
    @OneToMany(mappedBy = "job")
    List<JobCvReviewer> jobCvReviewers;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobDocument> documents;

}
