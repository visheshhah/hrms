package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class JobReferral extends BaseClass{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String comment;

    @Column(nullable = false)
    private String friendName;

    private String friendEmail;

    private String fileName;
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "document_type_id",  nullable = false)
    private DocumentType documentType;
}
