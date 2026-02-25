package com.example.hrms.controllers.social;

import com.example.hrms.dtos.social.TagsTypeDto;
import com.example.hrms.services.social.TagsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {
    private final TagsService tagsService;

    @GetMapping
    public ResponseEntity<List<TagsTypeDto>> getAllTags() {
        return ResponseEntity.ok(tagsService.findAll());
    }
}
