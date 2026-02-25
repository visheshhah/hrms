package com.example.hrms.repositories;

import com.example.hrms.entities.Tags;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagsRepository extends JpaRepository<Tags, Long> {
}
