package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Role;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    List<User> findAllByRolesName(ERole roleName);

    boolean existsByEmployee(Employee employee);
}