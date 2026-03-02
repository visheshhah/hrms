package com.example.hrms.repositories;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Post;
import com.example.hrms.enums.CelebrationType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface PostRepository extends JpaRepository<Post, Long> {
//    @EntityGraph(attributePaths = {"createdBy", "postTags", "postTags.tag"})
//    Page<Post> findByIsDeletedFalse(Pageable pageable);

    boolean existsByCelebrationEmployeeAndCelebrationDateAndCelebrationType(
            Employee celebrationEmployee,
            LocalDate celebrationDate,
            CelebrationType celebrationType
    );


    @EntityGraph(attributePaths = {"createdBy", "postTags", "postTags.tag"})
    @Query("""
       SELECT p FROM Post p
       WHERE p.isDeleted = false
       ORDER BY 
            CASE 
                WHEN p.celebrationDate = :today THEN 0
                ELSE 1
            END,
            p.createdAt DESC
       """)
    Page<Post> findFeedPosts(LocalDate today, Pageable pageable);


    @EntityGraph(attributePaths = {
            "createdBy",
            "postTags",
            "postTags.tag"
    })
    Page<Post> findByCreatedBy_IdAndIsDeletedFalse(
            Long employeeId,
            Pageable pageable
    );
}
