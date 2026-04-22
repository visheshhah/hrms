package com.example.hrms.controllers;

import com.example.hrms.dtos.documentType.DocumentTypeCreateDto;
import com.example.hrms.dtos.documentType.DocumentTypeResponseDto;
import com.example.hrms.dtos.documentType.DocumentTypeUpdateDto;
import com.example.hrms.entities.DocumentType;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.repositories.DocumentTypeRepository;
import com.example.hrms.services.document.DocumentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static com.example.hrms.utils.FileFormatConstants.VALID_FORMATS;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/document-types")
public class DocumentTypeController {
    private final DocumentTypeService documentTypeService;

    @GetMapping
    public ResponseEntity<List<DocumentType>> getAllDocumentTypes() {
        List<DocumentType> documentTypes = documentTypeService.getAllDocuments();
        return ResponseEntity.ok(documentTypes);
    }

    @GetMapping("/types")
    public ResponseEntity<List<DocumentTypeResponseDto>> getAll() {
        return ResponseEntity.ok(documentTypeService.getAll());
    }

    @PostMapping
    public ResponseEntity<DocumentTypeResponseDto> create(
            @RequestBody DocumentTypeCreateDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentTypeService.create(request));    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        documentTypeService.delete(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentTypeResponseDto> update(
            @PathVariable Long id,
            @RequestBody DocumentTypeUpdateDto request
    ) {
        return ResponseEntity.ok(documentTypeService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentTypeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentTypeService.getById(id));
    }

    @GetMapping("/formats")
    public ResponseEntity<Set<String>> getFormats() {
        return ResponseEntity.ok(VALID_FORMATS);
    }
}
