package com.example.hrms.controllers;

import com.example.hrms.dtos.department.DepartmentRequestDto;
import com.example.hrms.dtos.employee.DepartmentTypeDto;
import com.example.hrms.entities.Department;
import com.example.hrms.repositories.DepartmentRepository;
import com.example.hrms.services.department.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentTypeDto>> getAllDepartments() {
        List<DepartmentTypeDto> departments = departmentService.findAll();
        return ResponseEntity.ok(departments);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<DepartmentTypeDto> create(
            @Valid @RequestBody DepartmentRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.create(dto));
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentTypeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentTypeDto> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDto dto) {

        return ResponseEntity.ok(departmentService.update(id, dto));
    }

    // DELETE (soft)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
