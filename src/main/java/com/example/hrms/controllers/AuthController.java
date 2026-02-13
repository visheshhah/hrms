package com.example.hrms.controllers;

import com.example.hrms.dtos.AuthResponseDto;
import com.example.hrms.dtos.LoginDto;
import com.example.hrms.dtos.SignUpDto;
import com.example.hrms.services.AuthService;
import com.example.hrms.utils.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto> signup(@RequestBody SignUpDto signUpDTO) {
        AuthResponseDto userDTO = authService.signUp(signUpDTO);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto loginDTO, HttpServletResponse response) {
        String token = authService.login(loginDTO);

        return ResponseEntity.ok(token);
    }
}