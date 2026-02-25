package com.example.hrms.dtos.social;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeToggleResponseDto {

    private Boolean liked;
    private Long likeCount;

}