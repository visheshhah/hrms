package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ShareJob extends BaseClass{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shared_by_id")
    private Employee sharedBy;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    private String email;

}
