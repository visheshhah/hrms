package com.example.hrms.controllers;

import com.example.hrms.dtos.CurrentUserResponseDto;
import com.example.hrms.dtos.role.EmployeeRoleResponseDto;
import com.example.hrms.dtos.role.UpdateUserRoleDto;
import com.example.hrms.dtos.role.UserRoleDetailResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleManagementController {
    private final AuthService authService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<EmployeeRoleResponseDto>> getUserWithRoles(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(authService.getEmployeeAndRoles(userDetails.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/update/{user-id}")
    public ResponseEntity<Void> updateUserRoles(@PathVariable("user-id") Long userId, @RequestBody UpdateUserRoleDto userRoleDto){
        authService.updateUserRoles(userRoleDto, userId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserRoleDetailResponseDto> getUserRoles(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserRoleDetails(id));
    }
}
