package com.example.hrms.dtos.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class FileResponseDto {

    private Resource resource;
    private String originalFileName;
    private String contentType;
}