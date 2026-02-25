package com.example.hrms.dtos.social;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreatePostDto {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private List<PostTagDto> tags;

}
