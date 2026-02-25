package com.example.hrms.repositories;

import com.example.hrms.entities.PostTags;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostTagsRepository extends JpaRepository<PostTags, Long> {

    @Modifying
    @Query("DELETE FROM PostTags pt WHERE pt.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
