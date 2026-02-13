package com.example.hrms.repositories;

import com.example.hrms.entities.Role;
import com.example.hrms.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
