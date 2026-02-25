package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Post;
import com.example.hrms.enums.CelebrationType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"createdBy", "postTags", "postTags.tag"})
    Page<Post> findByIsDeletedFalse(Pageable pageable);

    boolean existsByCelebrationEmployeeAndCelebrationDateAndCelebrationType(
            Employee celebrationEmployee,
            LocalDate celebrationDate,
            CelebrationType celebrationType
    );

}
