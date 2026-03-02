package com.example.hrms.controllers.social;

import com.example.hrms.dtos.social.CreatePostDto;
import com.example.hrms.dtos.social.EditPostDto;
import com.example.hrms.dtos.social.PostResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.social.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<Long> post(@RequestBody CreatePostDto createPostDto, @AuthenticationPrincipal MyUserDetails userDetails){
        Long userId = userDetails.getId();
        return new ResponseEntity<>(postService.createPost(createPostDto,userId), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{post-id}")
    public ResponseEntity<Void> deletePost(@PathVariable("post-id") Long postId, @AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {
        Long userId = userDetails.getId();
        postService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        Long userId = userDetails.getId();
        return new ResponseEntity<>(postService.getPosts(page, size, userId), HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal MyUserDetails userDetails) {

        PostResponseDto response =
                postService.getPostById(postId, userDetails.getId());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> editPost(
            @PathVariable Long postId,
            @RequestBody @Valid EditPostDto dto,
            @AuthenticationPrincipal MyUserDetails userDetails) throws AccessDeniedException {

        PostResponseDto updatedPost =
                postService.editPost(postId, dto, userDetails.getId());

        return ResponseEntity.ok(updatedPost);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PostResponseDto>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {

        List<PostResponseDto> posts =
                postService.getMyPosts(userDetails.getId(), page, size);

        return ResponseEntity.ok(posts);
    }
}
