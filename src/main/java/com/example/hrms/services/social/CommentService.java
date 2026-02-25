package com.example.hrms.services.social;

import com.example.hrms.dtos.social.AddCommentDto;
import com.example.hrms.dtos.social.CommentResponseDto;
import com.example.hrms.dtos.social.EditCommentDto;
import com.example.hrms.entities.Comment;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Post;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.CommentRepository;
import com.example.hrms.repositories.PostRepository;
import com.example.hrms.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public Long addComment(AddCommentDto addCommentDto, Long userId, Long postId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new IllegalStateException("Cannot comment on deleted post");
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(user.getEmployee());
        comment.setCommentText(addCommentDto.getComment().trim());
        comment.setCreatedAt(Instant.now());

        return commentRepository.save(comment).getId();
    }

    public List<CommentResponseDto> getComments(Long postId, Long currentUserId) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isHr = currentUser.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new IllegalStateException("Post is deleted");
        }

        List<Comment> comments =
                commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtDesc(postId);

        return comments.stream()
                .map(comment -> {

                    CommentResponseDto dto = new CommentResponseDto();

                    dto.setCommentId(comment.getId());
                    dto.setCommentText(comment.getCommentText());
                    dto.setIsEdited(comment.getIsEdited());
                    dto.setEmployeeId(comment.getAuthor().getId());
                    dto.setEmployeeName(
                            comment.getAuthor().getFirstName() + " " +
                                    comment.getAuthor().getLastName()
                    );

                    dto.setCreatedAt(comment.getCreatedAt());

                    // 🔥 Permission logic
                    boolean isOwner =
                            comment.getAuthor().getId().equals(currentUser.getEmployee().getId());

                    dto.setCanEdit(isOwner); // Only owner edits
                    dto.setCanDelete(isOwner || isHr); // Owner OR HR deletes

                    return dto;
                })
                .toList();
    }

//    public void deleteComment(Long commentId, Long userId) {
//        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
//        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        Employee deletedBy = user.getEmployee();
//
//        comment.setDeletedBy(deletedBy);
//        comment.setIsDeleted(Boolean.TRUE);
//        comment.setDeletedAt(Instant.now());
//
//        commentRepository.save(comment);
//    }

    public void deleteComment(Long commentId, Long userId) throws AccessDeniedException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee deletedBy = user.getEmployee();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean isOwner = comment.getAuthor().getId().equals(deletedBy.getId());
        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);

        if (!isOwner && !isHr) {
            throw new AccessDeniedException("Not allowed to delete this comment");
        }

        comment.setIsDeleted(Boolean.TRUE);
        comment.setDeletedBy(user.getEmployee());
        comment.setDeletedAt(Instant.now());
    }

    @Transactional
    public CommentResponseDto editComment(Long commentId,
                                          EditCommentDto dto,
                                          Long currentUserId) throws AccessDeniedException {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new IllegalStateException("Cannot edit a deleted comment");
        }

        boolean isOwner =
                comment.getAuthor().getId().equals(currentUser.getEmployee().getId());

        if (!isOwner) {
            throw new AccessDeniedException("You are not allowed to edit this comment");
        }

        // 🔹 Update content
        comment.setCommentText(dto.getCommentText().trim());
        comment.setIsEdited(true);
        comment.setUpdatedAt(Instant.now());

        // 🔥 Build response DTO

        CommentResponseDto response = new CommentResponseDto();

        response.setCommentId(comment.getId());
        response.setCommentText(comment.getCommentText());

        response.setEmployeeId(comment.getAuthor().getId());
        response.setEmployeeName(
                comment.getAuthor().getFirstName() + " " +
                        comment.getAuthor().getLastName()
        );

        response.setCreatedAt(comment.getCreatedAt());
        response.setIsEdited(comment.getIsEdited());

        response.setCanEdit(true);
        response.setCanDelete(true); // owner can delete

        return response;
    }
}
