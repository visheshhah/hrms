package com.example.hrms.services;

import com.example.hrms.dtos.AuthResponseDto;
import com.example.hrms.dtos.LoginDto;
import com.example.hrms.dtos.SignUpDto;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
        if(userRepository.existsByUsername(signUpDTO.getUsername())){
            throw new BadCredentialsException("Username is already in use");
        }

        Employee employee = employeeRepository.findById(signUpDTO.getEmployeeId()).orElseThrow(()->new ResourceNotFoundException("Employee not found"));


        User user = User.builder()
                .username(signUpDTO.getUsername())
                .email(signUpDTO.getEmail())
                .password(passwordEncoder.encode(signUpDTO.getPassword()))
                .isActive(true)
                .build();

        Set<Role> roles = new HashSet<>();

        if(signUpDTO.getRoles() == null || signUpDTO.getRoles().isEmpty()){
            roles.add(roleRepository.findByName(ERole.ROLE_EMPLOYEE).orElseThrow(() -> new RuntimeException("Role Not Found")));
        }
        else {
            for (String role : signUpDTO.getRoles()) {
                switch (role.toLowerCase()) {
                    case "admin" -> roles.add(roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow());
                    case "hr" -> roles.add(roleRepository.findByName(ERole.ROLE_HR).orElseThrow());
                    case "manager" -> roles.add(roleRepository.findByName(ERole.ROLE_MANAGER).orElseThrow());

                    default -> roles.add(roleRepository.findByName(ERole.ROLE_EMPLOYEE).orElseThrow());


                }
            }
        }

        user.setEmployee(employee);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, AuthResponseDto.class);
    }

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
}