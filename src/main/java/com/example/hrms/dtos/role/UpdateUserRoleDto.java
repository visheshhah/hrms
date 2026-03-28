package com.example.hrms.dtos.role;

import com.example.hrms.enums.ERole;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateUserRoleDto {
        private Set<ERole> roles;

}
