package com.example.hrms.services;

import com.example.hrms.dtos.AuthResponseDto;
import com.example.hrms.dtos.CurrentUserResponseDto;
import com.example.hrms.dtos.LoginDto;
import com.example.hrms.dtos.SignUpDto;
import com.example.hrms.dtos.role.EmployeeRoleResponseDto;
import com.example.hrms.dtos.role.UpdateUserRoleDto;
import com.example.hrms.dtos.role.UserRoleDetailResponseDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Role;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.RoleRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;


    public AuthResponseDto signUp(SignUpDto signUpDTO) {

        if (userRepository.existsByUsername(signUpDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Employee employee = employeeRepository.findById(signUpDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (userRepository.existsByEmployee(employee)) {
            throw new RuntimeException("User already exists for this employee");
        }

        User user = User.builder()
                .username(signUpDTO.getUsername())
                .email(employee.getEmail())
                .password(passwordEncoder.encode(signUpDTO.getPassword()))
                .isActive(true)
                .build();

        Set<Role> roles = new HashSet<>();

        if (signUpDTO.getRoles() == null || signUpDTO.getRoles().isEmpty()) {
            roles.add(roleRepository.findByName(ERole.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new RuntimeException("Role not found")));
        } else {
            for (String role : signUpDTO.getRoles()) {
                try {
                    ERole enumRole = ERole.valueOf("ROLE_" + role.toUpperCase());
                    roles.add(roleRepository.findByName(enumRole)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + role)));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid role: " + role);
                }
            }
        }

        user.setEmployee(employee);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, AuthResponseDto.class);
    }
//    public AuthResponseDto signUp(SignUpDto signUpDTO) {
//        if(userRepository.existsByUsername(signUpDTO.getUsername())){
//            throw new BadCredentialsException("Username is already in use");
//        }
//
//        Employee employee = employeeRepository.findById(signUpDTO.getEmployeeId()).orElseThrow(()->new ResourceNotFoundException("Employee not found"));
//
//
//        User user = User.builder()
//                .username(signUpDTO.getUsername())
//                .email(signUpDTO.getEmail())
//                .password(passwordEncoder.encode(signUpDTO.getPassword()))
//                .isActive(true)
//                .build();
//
//        Set<Role> roles = new HashSet<>();
//
//        if(signUpDTO.getRoles() == null || signUpDTO.getRoles().isEmpty()){
//            roles.add(roleRepository.findByName(ERole.ROLE_EMPLOYEE).orElseThrow(() -> new RuntimeException("Role Not Found")));
//        }
//        else {
//            for (String role : signUpDTO.getRoles()) {
//                switch (role.toLowerCase()) {
//                    case "admin" -> roles.add(roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow());
//                    case "hr" -> roles.add(roleRepository.findByName(ERole.ROLE_HR).orElseThrow());
//                    case "manager" -> roles.add(roleRepository.findByName(ERole.ROLE_MANAGER).orElseThrow());
//                    case "reviewer" -> roles.add(roleRepository.findByName(ERole.ROLE_REVIEWER).orElseThrow());
//
//                    default -> roles.add(roleRepository.findByName(ERole.ROLE_EMPLOYEE).orElseThrow());
//
//
//                }
//            }
//        }
//
//        user.setEmployee(employee);
//        user.setRoles(roles);
//        User savedUser = userRepository.save(user);
//        return modelMapper.map(savedUser, AuthResponseDto.class);
//    }

    public String login(LoginDto loginDTO) {
        // try{
        //User user = userRepository.findByUsername(loginDTO.getUsername()).orElseThrow();
        //System.out.println("Db password" + user.getPassword());
        //boolean matches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        //System.out.println("Matches " + matches);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                );

        Authentication authentication = authenticationManager.authenticate(token);

        return jwtService.generateToken(authentication);
//        }catch (Exception e){
//            throw  new BadCredentialsException("Bad Credentials + "+e.getMessage());
//        }

    }

    @Transactional
    public void updateUserRoles(UpdateUserRoleDto request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Role> newRoles = request.getRoles().stream()
                .map(roleEnum -> roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);

        userRepository.save(user);
    }

    public List<EmployeeRoleResponseDto> getEmployeeAndRoles(Long userId) {
        User adminUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAdmin = adminUser.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_ADMIN);
        if(!isAdmin){
            throw new AccessDeniedException("You are not allowed to perform this action");
        }

        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {

            Employee employee = user.getEmployee();

            EmployeeRoleResponseDto dto = new EmployeeRoleResponseDto();
            dto.setId(user.getId());
            dto.setFirstName(employee.getFirstName());
            dto.setLastName(employee.getLastName());
            dto.setDesignation(employee.getDesignation());
            dto.setDepartment(employee.getDepartment().getDepartmentName());

            Set<ERole> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            dto.setRoles(roles);

            return dto;

        }).collect(Collectors.toList());
    }

    public UserRoleDetailResponseDto getUserRoleDetails(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<ERole> assignedRoles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<ERole> allRoles = Arrays.stream(ERole.values())
                .collect(Collectors.toSet());

        UserRoleDetailResponseDto dto = new UserRoleDetailResponseDto();
        dto.setUserId(user.getId());
        dto.setName(user.getEmployee().getFirstName() + " " + user.getEmployee().getLastName());
        dto.setAssignedRoles(assignedRoles);
        dto.setAllRoles(allRoles);

        return dto;
    }

    public CurrentUserResponseDto getCurrentUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Employee employee = user.getEmployee();

        CurrentUserResponseDto dto = new CurrentUserResponseDto();
        dto.setUserId(user.getId());
        dto.setEmployeeId(employee != null ? employee.getId() : null);
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(user.getEmail());

        Set<ERole> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        dto.setRoles(roles);

        return dto;
    }
}