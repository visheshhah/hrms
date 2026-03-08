package com.example.hrms.entities;

import com.example.hrms.enums.NotificationType;
import com.example.hrms.enums.ReferenceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Getter
@Setter
public class Notification extends BaseClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee receiver;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    private Boolean readStatus = false;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    private ReferenceType referenceType;

    private Long referenceId;

    private LocalDate notificationDate = LocalDate.now();

}