package com.example.hrms.controllers.social;

import com.example.hrms.dtos.social.AddCommentDto;
import com.example.hrms.dtos.social.LikeToggleResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.social.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/{postId}")
    public ResponseEntity<LikeToggleResponseDto> toogleLike(@PathVariable("postId") Long postId, @AuthenticationPrincipal MyUserDetails myUserDetails) {
        Long userId = myUserDetails.getId();
        return new ResponseEntity<>(likeService.toggleLike(postId, userId),HttpStatus.OK);
    }


}
