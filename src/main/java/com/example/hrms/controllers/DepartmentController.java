package com.example.hrms.controllers;

import com.example.hrms.dtos.employee.DepartmentTypeDto;
import com.example.hrms.entities.Department;
import com.example.hrms.repositories.DepartmentRepository;
import com.example.hrms.services.department.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentTypeDto>> getAllDepartments() {
        List<DepartmentTypeDto> departments = departmentService.getDepartments();
        return ResponseEntity.ok(departments);
    }
}
