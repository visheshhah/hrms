package com.example.hrms.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DocumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(unique = true, nullable = false)
    private String name;

//    @Column(nullable = false)
//    private Boolean isMandatory;

    private Boolean isActive = Boolean.TRUE;


    private String allowedFormats = "PDF,JPG,PNG";
}
