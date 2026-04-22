package com.example.hrms.controllers.social;

import com.example.hrms.dtos.social.TagsTypeDto;
import com.example.hrms.dtos.tag.TagRequestDto;
import com.example.hrms.services.social.TagsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<TagsTypeDto> create(@Valid @RequestBody TagRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tagsService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagsTypeDto> update(
            @PathVariable Long id,
            @Valid @RequestBody TagRequestDto dto) {

        return ResponseEntity.ok(tagsService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagsTypeDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tagsService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagsService.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
