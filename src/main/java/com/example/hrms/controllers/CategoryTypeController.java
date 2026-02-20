package com.example.hrms.controllers;

import com.example.hrms.dtos.expense.CategoryResponseDto;
import com.example.hrms.entities.CategoryType;
import com.example.hrms.services.expense.CategoryTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryTypeController {
    private final CategoryTypeService categoryTypeService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categoryResponseDtos = categoryTypeService.findAll();
        return ResponseEntity.ok().body(categoryResponseDtos);
    }
}
