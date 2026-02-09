package com.example.hrms.entities;

import com.example.hrms.enums.EOwnerType;
import jakarta.persistence.*;

@Entity
@Table(name = "travel_documents")
public class TravelDocument extends BaseClass{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EOwnerType ownerType;
}
