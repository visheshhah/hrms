package com.example.hrms.services.travel;

import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees
                .stream()
                .map(employee -> {
                    EmployeeDto employeeDto = new EmployeeDto();
                    employeeDto.setId(employee.getId());
                    employeeDto.setFirstName(employee.getFirstName());
                    employeeDto.setLastName(employee.getLastName());
                    employeeDto.setDesignation(employee.getDesignation());
                    employeeDto.setDepartment(employee.getDepartment().getDepartmentName());
                    return employeeDto;
                })
                .toList();
    }

    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("NO CUSTOMER PRESENT WITH ID = " + id));
        return modelMapper.map(employee, EmployeeDto.class);
    }

    public List<EmployeeDto> getEmployeesByManagerId(Long managerId) {
        Optional<Employee> employees = employeeRepository.findEmployeesByManagerId(managerId);
        return employees
                .stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .collect(Collectors.toList());

    }

    public List<EmployeeDto> getManagerialChain(Long employeeId) {
        if (employeeRepository.findById(employeeId).isEmpty()) {
            throw new ResourceNotFoundException("EMPLOYEE NOT FOUND");
        }
        List<Employee> chain = new ArrayList<>();
        Employee current = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        while (current != null) {
            chain.add(current);
            current = current.getManager();
        }


        return chain
                .stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .collect(Collectors.toList());
    }

}
