package com.example.hrms.controllers;

import com.example.hrms.dtos.categoryType.CategoryTypeUpdateDto;
import com.example.hrms.dtos.categoryType.ExpenseCategoryCreateDto;
import com.example.hrms.dtos.expense.CategoryResponseDto;
import com.example.hrms.entities.CategoryType;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.User;
import com.example.hrms.services.expense.CategoryTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createExpenseCategory(
            @Valid @RequestBody ExpenseCategoryCreateDto dto
    ) {
        CategoryResponseDto created = categoryTypeService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategoryType(
            @PathVariable Long id,
            @Valid @RequestBody CategoryTypeUpdateDto dto
    ) {
        CategoryResponseDto updated = categoryTypeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryType(@PathVariable Long id, @AuthenticationPrincipal User user) {
        categoryTypeService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
