package com.example.hrms.services.travel;

import com.example.hrms.dtos.employee.AddEmployeeDto;
import com.example.hrms.dtos.employee.EmployeeDetailResponseDto;
import com.example.hrms.dtos.employee.EmployeeProfileDto;
import com.example.hrms.dtos.employee.UpdateEmployeeProfileDto;
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

import java.time.Instant;
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
        List<Employee> employees = employeeRepository.findAll().stream().filter(employee -> !employee.getId().equals(10L)).toList();
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
        //boolean isAdmin = creator.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_ADMIN);

//        if(!isAdmin) {
//            throw new AccessDeniedException("You do not have access to this resource");
//        }

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

    public EmployeeDto getEmployeeDetail(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return modelMapper.map(user.getEmployee(), EmployeeDto.class);
    }

    public List<EmployeeDto> getEmployeesByManager(Long userId) {
        User user =  userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean isManager = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_MANAGER);
        if(!isManager){
            throw new AccessDeniedException("You are not allowed to perform this action");
        }

        List<Employee> employees = employeeRepository.findEmployeesByManagerId(user.getEmployee().getId()).orElse(new ArrayList<>());
        return employees
                .stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .toList();

    }

    public EmployeeDetailResponseDto getEmployeeProfile(Long userId){
        User  user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = user.getEmployee();

        EmployeeDetailResponseDto employeeDetailResponseDto = new EmployeeDetailResponseDto();
        employeeDetailResponseDto.setId(employee.getId());
        employeeDetailResponseDto.setFullName(employee.getFirstName() + " " + employee.getLastName());
        employeeDetailResponseDto.setEmail(employee.getEmail());
        employeeDetailResponseDto.setJoiningDate(employee.getJoiningDate());
        employeeDetailResponseDto.setDesignation(employee.getDesignation());
        employeeDetailResponseDto.setPhoneNumber(employee.getPhoneNumber());
        employeeDetailResponseDto.setDepartmentName(employee.getDepartment().getDepartmentName());
        employeeDetailResponseDto.setDateOfBirth(employee.getDateOfBirth());
        if(employee.getManager() != null){
            employeeDetailResponseDto.setManagerName(employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
        }
        return employeeDetailResponseDto;

    }

    public void updateEmployeeProfile(Long employeeId, Long userId, UpdateEmployeeProfileDto updateEmployeeProfileDto){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        String firstName = updateEmployeeProfileDto.getFirstName();
        if (firstName != null && !firstName.trim().isEmpty()) {
            employee.setFirstName(firstName.trim());
        }

        String lastName = updateEmployeeProfileDto.getLastName();
        if (lastName != null && !lastName.trim().isEmpty()) {
            employee.setLastName(lastName.trim());
        }

        String email = updateEmployeeProfileDto.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            employee.setEmail(email.trim());
        }

        if (updateEmployeeProfileDto.getPhoneNumber() != null
                && !updateEmployeeProfileDto.getPhoneNumber().trim().isEmpty()) {
            employee.setPhoneNumber(updateEmployeeProfileDto.getPhoneNumber().trim());
        }

        if (updateEmployeeProfileDto.getJoiningDate() != null) {
            employee.setJoiningDate(updateEmployeeProfileDto.getJoiningDate());
        }

        if (updateEmployeeProfileDto.getDateOfBirth() != null) {
            employee.setDateOfBirth(updateEmployeeProfileDto.getDateOfBirth());
        }

        if (updateEmployeeProfileDto.getSalary() != null) {
            employee.setSalary(updateEmployeeProfileDto.getSalary());
        }

        if (updateEmployeeProfileDto.getDesignation() != null
                && !updateEmployeeProfileDto.getDesignation().trim().isEmpty()) {
            employee.setDesignation(updateEmployeeProfileDto.getDesignation().trim());
        }

        if (updateEmployeeProfileDto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(updateEmployeeProfileDto.getDepartmentId()).orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employee.setDepartment(department);
        }

        if (updateEmployeeProfileDto.getManagerId() != null) {
            Employee manager = employeeRepository.findById(updateEmployeeProfileDto.getManagerId()).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            employee.setManager(manager);
        }

        employeeRepository.save(employee);
    }

    public EmployeeProfileDto getEmployeeProfileDetail(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        EmployeeProfileDto employeeProfileDto = new EmployeeProfileDto();
        employeeProfileDto.setEmployeeId(employee.getId());
        employeeProfileDto.setFirstName(employee.getFirstName());
        employeeProfileDto.setLastName(employee.getLastName());
        employeeProfileDto.setEmail(employee.getEmail());
        employeeProfileDto.setPhoneNumber(employee.getPhoneNumber());
        employeeProfileDto.setJoiningDate(employee.getJoiningDate());
        employeeProfileDto.setDateOfBirth(employee.getDateOfBirth());
        employeeProfileDto.setSalary(employee.getSalary());
        employeeProfileDto.setDesignation(employee.getDesignation());
        employeeProfileDto.setDepartmentId(employee.getDepartment().getId());
        if (employee.getManager() != null) {

            employeeProfileDto.setManagerId(employee.getManager().getId());
        }
        return employeeProfileDto;

    }

    public List<EmployeeDetailResponseDto> getAllEmployeesProfile(){
        List<Employee> employees = employeeRepository.findAllEmployees();

        return employees.stream()
                .map(employee -> {

                    EmployeeDetailResponseDto employeeDetailResponseDto = new EmployeeDetailResponseDto();
                    employeeDetailResponseDto.setId(employee.getId());
                    employeeDetailResponseDto.setFullName(employee.getFirstName() + " " + employee.getLastName());
                    employeeDetailResponseDto.setEmail(employee.getEmail());
                    employeeDetailResponseDto.setJoiningDate(employee.getJoiningDate());
                    employeeDetailResponseDto.setDesignation(employee.getDesignation());
                    employeeDetailResponseDto.setPhoneNumber(employee.getPhoneNumber());
                    employeeDetailResponseDto.setDepartmentName(employee.getDepartment().getDepartmentName());
                    employeeDetailResponseDto.setDateOfBirth(employee.getDateOfBirth());
                    if(employee.getManager() != null){
                        employeeDetailResponseDto.setManagerName(employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
                    }
                        return employeeDetailResponseDto;
                }).toList();

    }

    public void deleteEmployee(Long employeeId, Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        employee.setDeletedAt(Instant.now());
        employee.setDeletedBy(user.getEmployee());
        employee.setIsDeleted(Boolean.TRUE);

        employeeRepository.save(employee);
    }

    public List<EmployeeDetailResponseDto> getAvailableEmployees() {
        List<Employee> employees = employeeRepository.findAvailableEmployees();

        return employees.stream()
                .map(employee -> {
                    EmployeeDetailResponseDto employeeDetailResponseDto = new EmployeeDetailResponseDto();
                    employeeDetailResponseDto.setId(employee.getId());
                    employeeDetailResponseDto.setFullName(employee.getFirstName() + " " + employee.getLastName());
                    employeeDetailResponseDto.setEmail(employee.getEmail());
                    employeeDetailResponseDto.setJoiningDate(employee.getJoiningDate());
                    employeeDetailResponseDto.setDesignation(employee.getDesignation());
                    employeeDetailResponseDto.setPhoneNumber(employee.getPhoneNumber());
                    employeeDetailResponseDto.setDepartmentName(employee.getDepartment().getDepartmentName());
                    employeeDetailResponseDto.setDateOfBirth(employee.getDateOfBirth());
                    if(employee.getManager() != null){
                        employeeDetailResponseDto.setManagerName(employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
                    }
                    return employeeDetailResponseDto;
                }).toList();
    }

}
