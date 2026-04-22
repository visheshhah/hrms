package com.example.hrms.exceptions;

import com.example.hrms.dtos.ErrorResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException ex) {

        String message = "Duplicate value already exists";

        if (ex.getMessage() != null) {
            if (ex.getMessage().toLowerCase().contains("name")) {
                message = "Name already exists";
            } else if (ex.getMessage().toLowerCase().contains("code")) {
                message = "Code already exists";
            }
        }

        ErrorResponseDto error = new ErrorResponseDto(
                message,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TravelConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleTravelConflict(TravelConflictException ex) {

        List<Map<String, Object>> employees = ex.getEmployees().stream()
                .map(emp -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", emp.getId());
                    map.put("name", emp.getFirstName() + " " + emp.getLastName());
                    return map;
                })
                .toList();

        ErrorResponseDto error = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                employees
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmployeeTravelConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleEmployeeTravelConflict(EmployeeTravelConflictException ex) {

        ErrorResponseDto error = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now(),
                ex.getConflicts()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleDailyLimitExceeded(DailyLimitExceededException ex) {

        ErrorResponseDto error = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now(),
                ex.getEmployees()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(IllegalArgumentException ex) {
        ErrorResponseDto error = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}