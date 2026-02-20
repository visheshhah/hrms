package com.example.hrms.services.document;

import com.example.hrms.entities.DocumentType;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentTypeService {
    private final DocumentTypeRepository documentTypeRepository;
    private final ModelMapper modelMapper;

    public List<DocumentType> getAllDocuments() {
        return documentTypeRepository.findAll();
    }
}
