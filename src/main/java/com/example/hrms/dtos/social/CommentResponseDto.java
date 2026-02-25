package com.example.hrms.dtos.social;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CommentResponseDto {
    private Long commentId;
    private String commentText;

    private Long employeeId;
    private String employeeName;

    private Instant createdAt;

    private Boolean isEdited;

    private Boolean canEdit;
    private Boolean canDelete;
}
