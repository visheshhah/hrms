package com.example.hrms.services.department;

import com.example.hrms.dtos.employee.DepartmentTypeDto;
import com.example.hrms.entities.Department;
import com.example.hrms.repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    public List<DepartmentTypeDto> getDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream()
                .map(department -> modelMapper.map(department, DepartmentTypeDto.class))
                .toList();
    }
}
