package com.example.hrms.controllers;

import com.example.hrms.dtos.employee.AddEmployeeDto;
import com.example.hrms.dtos.employee.EmployeeDetailResponseDto;
import com.example.hrms.dtos.employee.EmployeeProfileDto;
import com.example.hrms.dtos.employee.UpdateEmployeeProfileDto;
import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.services.travel.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PreAuthorize("hasRole('HR')")
    @PostMapping("/create")
    public ResponseEntity<Long> createEmployee(@RequestBody AddEmployeeDto addEmployeeDto, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long userId = userDetails.getId();
        return new ResponseEntity<>(employeeService.addEmployee(addEmployeeDto, userId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> findAll() {

        List<EmployeeDto> employeeList = employeeService.getAllEmployees();
        if (employeeList.isEmpty()) {
            throw new ResourceNotFoundException("Employee not found");
        }
        return new ResponseEntity<>(employeeList, HttpStatus.OK);

    }

    @GetMapping("/{Id}")
    public ResponseEntity<EmployeeDto> findById(@PathVariable("Id") Long id) {
        EmployeeDto employeeDto = employeeService.getEmployeeById(id);
        return new ResponseEntity<>(employeeDto, HttpStatus.OK);
    }

    @GetMapping("/manager/{Id}")
    public ResponseEntity<List<EmployeeDto>> findEmployeesByManagerId(@PathVariable("Id") Long id) {
        List<EmployeeDto> employeeDtoList = employeeService.getEmployeesByManagerId(id);
        return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{id}/manager-chain")
    public ResponseEntity<List<EmployeeDto>> getManagerChain(@PathVariable Long id) {
        List<EmployeeDto> employeeDtoList = employeeService.getManagerialChain(id);
        return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);

    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeDto> getEmployee(@AuthenticationPrincipal MyUserDetails userDetails) {
        return new ResponseEntity<>(employeeService.getEmployeeDetail(userDetails.getId()), HttpStatus.OK);
    }

    @GetMapping("/manager/employees")
    public ResponseEntity<List<EmployeeDto>> findEmployeesByManager(@AuthenticationPrincipal MyUserDetails userDetails) {
        List<EmployeeDto> employeeDtoList = employeeService.getEmployeesByManager(userDetails.getId());
        return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<EmployeeDetailResponseDto> findEmployeeProfile(@AuthenticationPrincipal MyUserDetails userDetails) {
        EmployeeDetailResponseDto employeeDto = employeeService.getEmployeeProfile(userDetails.getId());
        return new ResponseEntity<>(employeeDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HR')")
    @PatchMapping("/update/{employee-id}")
    public ResponseEntity<Void> updateEmployeeProfile(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable("employee-id") Long employeeId, @RequestBody UpdateEmployeeProfileDto  updateEmployeeProfileDto) {
        employeeService.updateEmployeeProfile(employeeId, userDetails.getId(), updateEmployeeProfileDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/profile/{employee-id}")
    public ResponseEntity<EmployeeProfileDto> findEmployeeProfileById(@PathVariable("employee-id") Long employeeId,@AuthenticationPrincipal MyUserDetails userDetails) {
        EmployeeProfileDto employeeDto = employeeService.getEmployeeProfileDetail(employeeId);
        return new ResponseEntity<>(employeeDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/profiles")
    public ResponseEntity<List<EmployeeDetailResponseDto>> findEmployeessProfile() {
        List<EmployeeDetailResponseDto> employeeDtoList = employeeService.getAllEmployeesProfile();
        return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HR')")
    @DeleteMapping("/delete/{employee-id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("employee-id") Long employeeId, @AuthenticationPrincipal MyUserDetails userDetails) {
        employeeService.deleteEmployee(employeeId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-employees")
    public ResponseEntity<List<EmployeeDetailResponseDto>> getAvailableEmployees() {
        return new ResponseEntity<>(employeeService.getAvailableEmployees(),  HttpStatus.OK);
    }
}
