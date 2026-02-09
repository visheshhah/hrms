package com.example.hrms.entities;

import com.example.hrms.enums.TravelStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.security.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
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
    private boolean isInternational;

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
