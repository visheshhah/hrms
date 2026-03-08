package com.example.hrms.dtos.notification;

import com.example.hrms.entities.Employee;
import com.example.hrms.enums.NotificationType;
import com.example.hrms.enums.ReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNotificationDto {
    private Employee sender;

    @NotNull
    private Employee receiver;

    @NotNull
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private NotificationType notificationType;

    @NotNull
    private Long referenceId;

    @NotNull
    private ReferenceType referenceType;
}
