package com.example.hrms.services.job;

import com.example.hrms.dtos.travel.EmployeeDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Role;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.JobCvReviewerRepository;
import com.example.hrms.repositories.RoleRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobCvReviewerService {
    private final JobCvReviewerRepository jobCvReviewerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDto> getAllReviewers(){
        roleRepository.findByName(ERole.ROLE_REVIEWER).orElseThrow(() -> new ResourceNotFoundException("Role Not Found"));
        List<User> reviewers = userRepository.findAllByRolesName(ERole.ROLE_REVIEWER);

        List<Employee> employees = reviewers.stream()
                .map(User::getEmployee)
                .toList();

        return employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .toList();
    }


}
