package com.example.hrms.controllers.social;

import com.example.hrms.dtos.social.AddCommentDto;
import com.example.hrms.dtos.social.CommentResponseDto;
import com.example.hrms.dtos.social.EditCommentDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.repositories.CommentRepository;
import com.example.hrms.services.social.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("{postId}/add")
    public ResponseEntity<Long> addComment(@PathVariable("postId") Long postId, @RequestBody AddCommentDto addCommentDto, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        Long userId = myUserDetails.getId();
        return new ResponseEntity<>(commentService.addComment(addCommentDto, userId, postId), HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentByPostId(@PathVariable("postId") Long postId, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        Long userId = myUserDetails.getId();
        return new ResponseEntity<>(commentService.getComments(postId, userId), HttpStatus.OK);
    }

    @DeleteMapping("/{commentId}/delete")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal MyUserDetails myUserDetails) throws AccessDeniedException {
        commentService.deleteComment(commentId, myUserDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> editComment(
            @PathVariable Long commentId,
            @RequestBody @Valid EditCommentDto dto,
            @AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {

        CommentResponseDto response =
                commentService.editComment(commentId, dto, userDetails.getId());

        return ResponseEntity.ok(response);
    }
}
