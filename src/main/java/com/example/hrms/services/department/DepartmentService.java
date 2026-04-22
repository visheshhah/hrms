package com.example.hrms.services.department;

import com.example.hrms.dtos.department.DepartmentRequestDto;
import com.example.hrms.dtos.employee.DepartmentTypeDto;
import com.example.hrms.entities.Department;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

//    public List<DepartmentTypeDto> getDepartments() {
//        List<Department> departments = departmentRepository.findAll();
//        return departments.stream()
//                .map(department -> modelMapper.map(department, DepartmentTypeDto.class))
//                .toList();
//    }

    public DepartmentTypeDto create(DepartmentRequestDto dto) {

        Optional<Department> existing =
                departmentRepository.findByDepartmentName(dto.getDepartmentName());

        if (existing.isPresent()) {
            Department dept = existing.get();

            if (!dept.getIsActive()) {
                dept.setIsActive(true); // restore
                return modelMapper.map(departmentRepository.save(dept), DepartmentTypeDto.class);
            }

            throw new RuntimeException("Department already exists");
        }

        Department dept = new Department();
        dept.setDepartmentName(dto.getDepartmentName());

        return modelMapper.map(departmentRepository.save(dept), DepartmentTypeDto.class);
    }

    // ✅ GET ALL
    public List<DepartmentTypeDto> findAll() {
        return departmentRepository.findByIsActiveTrue()
                .stream()
                .map(d -> modelMapper.map(d, DepartmentTypeDto.class))
                .toList();
    }

    // ✅ GET BY ID
    public DepartmentTypeDto findById(Long id) {
        Department dept = departmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        return modelMapper.map(dept, DepartmentTypeDto.class);
    }

    // ✅ UPDATE
    public DepartmentTypeDto update(Long id, DepartmentRequestDto dto) {

        Department dept = departmentRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Optional<Department> existing =
                departmentRepository.findByDepartmentName(dto.getDepartmentName());

        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new RuntimeException("Department name already exists");
        }

        dept.setDepartmentName(dto.getDepartmentName());

        return modelMapper.map(departmentRepository.save(dept), DepartmentTypeDto.class);
    }

    // ✅ DELETE (soft + safety with employees)
    public void delete(Long id) {

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (!dept.getIsActive()) {
            throw new RuntimeException("Department already deleted");
        }

        // 🔥 IMPORTANT: prevent delete if employees exist
        if (dept.getEmployee() != null && !dept.getEmployee().isEmpty()) {
            throw new RuntimeException("Cannot delete department with assigned employees");
        }

        dept.setIsActive(false);
        departmentRepository.save(dept);
    }
}
