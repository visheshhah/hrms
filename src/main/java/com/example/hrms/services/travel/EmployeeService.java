package com.example.hrms.services.travel;

import com.example.hrms.dtos.employee.AddEmployeeDto;
import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.entities.Department;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.DepartmentRepository;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees
                .stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .toList();
    }

    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("NO CUSTOMER PRESENT WITH ID = " + id));
        return modelMapper.map(employee, EmployeeDto.class);
    }

    public List<EmployeeDto> getEmployeesByManagerId(Long managerId) {
        List<Employee> employees = employeeRepository.findEmployeesByManagerId(managerId).orElse(new ArrayList<>());
        return employees
                .stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .toList();

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

        Collections.reverse(chain);

        return chain
                .stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .toList();
    }

    @Transactional
    public Long addEmployee(AddEmployeeDto employeeDto, Long userId) {
        User creator = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isAdmin = creator.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_ADMIN);

        if(!isAdmin) {
            throw new AccessDeniedException("You do not have access to this resource");
        }

        Department department = departmentRepository.findById(employeeDto.getDepartmentId()).orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Employee manager = employeeRepository.findById(employeeDto.getManagerId()).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        Employee employee = new Employee();
        employee.setDateOfBirth(employeeDto.getDateOfBirth());
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setEmail(employeeDto.getEmail());
        employee.setJoiningDate(employeeDto.getJoiningDate());
        employee.setDesignation(employeeDto.getDesignation());
        employee.setPhoneNumber(employeeDto.getPhoneNumber());
        employee.setSalary(employeeDto.getSalary());
        employee.setManager(manager);
        employee.setDepartment(department);


        Employee savedEmployee = employeeRepository.save(employee);

        return savedEmployee.getId();

    }

}
