package com.example.hrms.services.social;

import com.example.hrms.dtos.social.LikeDto;
import com.example.hrms.dtos.social.LikeToggleResponseDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Like;
import com.example.hrms.entities.Post;
import com.example.hrms.entities.User;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.LikeRepository;
import com.example.hrms.repositories.PostRepository;
import com.example.hrms.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private  final UserRepository userRepository;
    private final PostRepository postRepository;

    public Long addLike(Long postId, Long userId) {
        User user =  userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        Employee employee = user.getEmployee();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new IllegalStateException("Cannot like a deleted post");
        }

        Like like = new Like();
        like.setPost(post);
        like.setLikedBy(employee);

        return likeRepository.save(like).getId();
    }

    @Transactional
    public LikeToggleResponseDto toggleLike(Long postId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Employee employee = user.getEmployee();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new IllegalStateException("Cannot like a deleted post");
        }

        Optional<Like> existingLike =
                likeRepository.findByPostIdAndLikedById(postId, employee.getId());

        boolean liked;

        if (existingLike.isPresent()) {
            // 🔥 Unlike (hard delete)
            likeRepository.delete(existingLike.get());
            liked = false;
        } else {
            // 🔥 Like
            Like like = new Like();
            like.setPost(post);
            like.setLikedBy(employee);
            likeRepository.save(like);
            liked = true;
        }

        // 🔥 Get updated count
        Long updatedCount = likeRepository.countByPostId(postId);

        return new LikeToggleResponseDto(liked, updatedCount);
    }
}
