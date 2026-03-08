package com.example.hrms.dtos.notification;

import com.example.hrms.enums.ReferenceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class NotificationDetailDto {
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String message;

    private ReferenceType referenceType;

    private Long referenceId;

    private String sender;
    private Instant createdAt;

}
