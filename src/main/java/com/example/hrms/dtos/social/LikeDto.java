package com.example.hrms.dtos.social;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeDto {
    @NotBlank
    private Long postId;
}
