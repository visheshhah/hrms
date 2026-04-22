package com.example.hrms.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {
    private String message;
    private int code;
    private LocalDateTime timestamp;
    private Object data;

    public ErrorResponseDto(String message, int code, LocalDateTime timestamp) {
        this.message = message;
        this.code = code;
        this.timestamp = timestamp;
    }

    public ErrorResponseDto(String message, int code, LocalDateTime timestamp, Object data) {
        this.message = message;
        this.code = code;
        this.timestamp = timestamp;
        this.data = data;
    }
}

