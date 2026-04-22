package com.example.hrms.repositories;

import com.example.hrms.entities.Tags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagsRepository extends JpaRepository<Tags, Long> {
    boolean existsByTagName(String tagName);

    List<Tags> findByIsActiveTrue();

    Optional<Tags> findByIdAndIsActiveTrue(Long id);

    Optional<Tags> findByTagName(String tagName);
}
