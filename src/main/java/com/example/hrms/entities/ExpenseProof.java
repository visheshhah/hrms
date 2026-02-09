package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Entity
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "expense_proofs")
public class ExpenseProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id")
    private DocumentType documentType;

    private String fileType;

    @Column(nullable = false)
    private Instant uploadedAt =  Instant.now();


}
