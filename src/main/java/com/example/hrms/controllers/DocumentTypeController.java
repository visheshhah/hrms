package com.example.hrms.controllers;

import com.example.hrms.entities.DocumentType;
import com.example.hrms.repositories.DocumentTypeRepository;
import com.example.hrms.services.document.DocumentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
