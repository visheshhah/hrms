package com.example.hrms.repositories;

import com.example.hrms.dtos.social.PostCountDto;
import com.example.hrms.entities.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Long countByPostIdAndIsDeletedFalse(Long postId);

    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtDesc(Long postId);

    @Query("""
           SELECT new com.example.hrms.dtos.social.PostCountDto(
                c.post.id,
                COUNT(c)
           )
           FROM Comment c
           WHERE c.post.id IN :postIds
           AND c.isDeleted = false
           GROUP BY c.post.id
           """)
    List<PostCountDto> countCommentsByPostIds(List<Long> postIds);


}
