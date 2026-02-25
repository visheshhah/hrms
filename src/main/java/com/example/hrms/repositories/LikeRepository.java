package com.example.hrms.repositories;

import com.example.hrms.dtos.social.PostCountDto;
import com.example.hrms.entities.Like;
import com.example.hrms.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Long countByPostId(Long postId);

    @Query("""
           SELECT new com.example.hrms.dtos.social.PostCountDto(
                l.post.id,
                COUNT(l)
           )
           FROM Like l
           WHERE l.post.id IN :postIds
           GROUP BY l.post.id
           """)
    List<PostCountDto> countLikesByPostIds(List<Long> postIds);

    Optional<Like> findByPostIdAndLikedById(Long postId, Long employeeId);

    @Query("""
       SELECT l.post.id
       FROM Like l
       WHERE l.post.id IN :postIds
       AND l.likedBy.id = :employeeId
       """)
    List<Long> findLikedPostIdsByUser(List<Long> postIds, Long employeeId);

    boolean existsByPostIdAndLikedById(Long postId, Long employeeId);
}
