package com.example.hrms.controllers;

import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.services.travel.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

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

}
