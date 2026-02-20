package com.example.hrms.entities;

import com.example.hrms.enums.EOwnerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "travel_documents")
public class TravelDocument{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    private TravelPlan travelPlan;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_employee_id", nullable = false)
    private Employee uploadedBy;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EOwnerType ownerType;

    private String fileName;

    private String filePath;

    @Column(nullable = false)
    private Instant uploadedAt = Instant.now();

}
