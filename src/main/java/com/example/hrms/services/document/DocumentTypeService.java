package com.example.hrms.services.document;

import com.example.hrms.dtos.documentType.DocumentTypeCreateDto;
import com.example.hrms.dtos.documentType.DocumentTypeResponseDto;
import com.example.hrms.dtos.documentType.DocumentTypeUpdateDto;
import com.example.hrms.entities.DocumentType;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.DocumentTypeRepository;
import com.example.hrms.repositories.TravelDocumentRepository;
import com.example.hrms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.hrms.utils.FileFormatConstants.VALID_FORMATS;

@Service
@RequiredArgsConstructor
public class DocumentTypeService {
    private final DocumentTypeRepository documentTypeRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final TravelDocumentRepository travelDocumentRepository;

    private static final String DEFAULT_FORMATS = "PDF,JPG,PNG";


    public List<DocumentType> getAllDocuments() {
        return documentTypeRepository.findAll();
    }

    public List<DocumentTypeResponseDto> getAll() {
        return documentTypeRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DocumentTypeResponseDto getById(Long id) {
        DocumentType entity = documentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DocumentType not found with id: " + id));

        if (entity.getDeletedAt() != null) {
            throw new IllegalArgumentException("DocumentType not found with id: " + id);
        }

        return mapToResponse(entity);
    }

    public DocumentTypeResponseDto create(DocumentTypeCreateDto request) {

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Code is required");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        Set<String> normalizedFormats = validateAndNormalizeFormats(request.getAllowedFormats());
        String formatsString = String.join(",", normalizedFormats);

        // 🔥 ONLY check by code
        Optional<DocumentType> existingOpt = documentTypeRepository.findByCodeIgnoreCase(code);

        if (existingOpt.isPresent()) {
            DocumentType existing = existingOpt.get();

            if (existing.getDeletedAt() != null) {
                // ✅ RESTORE (code stays same)
                existing.setDeletedAt(null);
                existing.setDeletedBy(null);
                existing.setIsActive(true);
                existing.setName(name); // name can change
                existing.setAllowedFormats(formatsString);

                return mapToResponse(documentTypeRepository.save(existing));
            } else {
                throw new IllegalArgumentException("Code already exists");
            }
        }

        // 🔹 validate name uniqueness (active only)
        if (documentTypeRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(name)) {
            throw new IllegalArgumentException("Name already exists");
        }

        // 🔹 create new
        DocumentType entity = new DocumentType();
        entity.setCode(code);
        entity.setName(name);
        entity.setAllowedFormats(formatsString);
        entity.setIsActive(true);

        return mapToResponse(documentTypeRepository.save(entity));
    }

    public void delete(Long id, Long userId) {

        DocumentType entity = documentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DocumentType not found with id: " + id));

        // Already deleted check (optional but good)
        if (entity.getDeletedAt() != null) {
            throw new IllegalArgumentException("DocumentType already deleted");
        }

        // Set soft delete fields
        entity.setDeletedAt(Instant.now());

        if (userId != null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            entity.setDeletedBy(user.getEmployee());
        }

        // Optionally deactivate
        entity.setIsActive(false);

        documentTypeRepository.save(entity);
    }

    public DocumentTypeResponseDto update(Long id, DocumentTypeUpdateDto request) {

        // 1. Fetch (only active)
        DocumentType entity = documentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DocumentType not found with id: " + id));

        if (entity.getDeletedAt() != null) {
            throw new IllegalArgumentException("Cannot update a deleted DocumentType");
        }

        // 2. Validate name
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        String name = request.getName().trim();

        // 3. Check name uniqueness (exclude current)
        if (!entity.getName().equalsIgnoreCase(name)
                && documentTypeRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(name)) {
            throw new IllegalArgumentException("Name already exists");
        }

        // 4. Validate formats (use your helper)
        Set<String> normalizedFormats = validateAndNormalizeFormats(request.getAllowedFormats());
        String formatsString = String.join(",", normalizedFormats);

        // 5. Update allowed fields ONLY
        entity.setName(name);
        entity.setAllowedFormats(formatsString);

        // ❗ code is NOT updated (immutable)

        // 6. Save
        DocumentType updated = documentTypeRepository.save(entity);

        return mapToResponse(updated);
    }

    private DocumentTypeResponseDto mapToResponse(DocumentType entity) {

        DocumentTypeResponseDto res = new DocumentTypeResponseDto();

        res.setId(entity.getId());
        res.setCode(entity.getCode());
        res.setName(entity.getName());
        res.setIsActive(entity.getIsActive());

        // convert String → Set
        if (entity.getAllowedFormats() != null && !entity.getAllowedFormats().isEmpty()) {
            Set<String> formats = Arrays.stream(entity.getAllowedFormats().split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            res.setAllowedFormats(formats);
        }

        return res;
    }

    private Set<String> validateAndNormalizeFormats(Set<String> formats) {
        if (formats == null || formats.isEmpty()) {
            throw new IllegalArgumentException("At least one format is required");
        }

        Set<String> normalized = formats.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        for (String format : normalized) {
            if (!VALID_FORMATS.contains(format)) {
                throw new IllegalArgumentException("Invalid format: " + format);
            }
        }

        return normalized;
    }
}
