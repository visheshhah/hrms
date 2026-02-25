package com.example.hrms.dtos.social;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostResponseDto {
    private Long id;
    private String title;
    private String description;
    private Long employeeId;
    private String authorName;
    private Instant createdAt;

    private Long likeCount;
    private Long commentCount;
    private Boolean isLikedByCurrentUser;

    // 🔹 Permission Flags
    private Boolean canEdit;
    private Boolean canDelete;

    private Boolean isSystemGenerated;


    private List<TagsTypeDto> postTags;

}
