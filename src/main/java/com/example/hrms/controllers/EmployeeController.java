package com.example.hrms.controllers;

import com.example.hrms.dtos.employee.AddEmployeeDto;
import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.services.travel.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

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

    @GetMapping("/{id}/manager-chain")
    public ResponseEntity<List<EmployeeDto>> getManagerChain(@PathVariable Long id) {
        List<EmployeeDto> employeeDtoList = employeeService.getManagerialChain(id);
        return new ResponseEntity<>(employeeDtoList, HttpStatus.OK);

    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeDto> getEmployee(@AuthenticationPrincipal MyUserDetails userDetails) {
        return new ResponseEntity<>(employeeService.getEmployeeDetail(userDetails.getId()), HttpStatus.OK);
    }

}
