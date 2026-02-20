package com.example.hrms.entities;

import com.example.hrms.enums.TravelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "travel_plan")
public class TravelPlan extends BaseClass{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private TravelStatus status = TravelStatus.DRAFT;

    @Column(nullable = false)
    private String sourceLocation;

    @Column(nullable = false)
    private String destinationLocation;

    @Column(nullable = false)
    private Boolean isInternational;

    private Boolean isActive = Boolean.TRUE;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "created_by_employee_id", nullable = false)
    private Employee createdByEmployee;

    @OneToMany(mappedBy = "travelPlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EmployeeTravel> employeeTravels = new ArrayList<>();

    @OneToMany(mappedBy = "travelPlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Expense>  expenses = new ArrayList<>();

    @OneToMany(mappedBy = "travelPlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TravelDocument>  travelDocuments = new ArrayList<>();

}
