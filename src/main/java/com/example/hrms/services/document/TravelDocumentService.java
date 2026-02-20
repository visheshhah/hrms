package com.example.hrms.services.document;

import com.example.hrms.dtos.document.UploadTravelDocumentRequestDto;
import com.example.hrms.entities.*;
import com.example.hrms.enums.EOwnerType;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import com.example.hrms.services.files.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class TravelDocumentService {
    private final TravelDocumentRepository travelDocumentRepository;
    private final EmployeeRepository employeeRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final EmployeeTravelRepository employeeTravelRepository;
    private final UserRepository userRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final FileStorageService fileStorageService;

    public Long uploadDocument(Long travelPlanId, UploadTravelDocumentRequestDto uploadTravelDocumentRequestDto, MultipartFile file, Long userId) {
        validateFilePresence(file);
        User uploader = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
        Employee uploadedByEmployee = uploader.getEmployee();


        boolean isHr = uploader.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));

        DocumentType documentType = documentTypeRepository.findById(uploadTravelDocumentRequestDto.getDocumentTypeId()).orElseThrow(() -> new ResourceNotFoundException("NO DOCUMENT TYPE FOUND"));

        validateFileFormat(file, documentType);

        Employee documentOwner = null;

        if(isHr) {
            if (uploadTravelDocumentRequestDto.getEmployeeId() != null) {
                documentOwner = employeeRepository.findById(uploadTravelDocumentRequestDto.getEmployeeId()).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
                validateParticipant(travelPlanId, documentOwner.getId());

            }
        }else {
                documentOwner = uploadedByEmployee;
                validateParticipant(travelPlanId, uploadedByEmployee.getId());
        }
        String storedFileName = fileStorageService.store(file, "travel-documents");

        TravelDocument travelDocument = new TravelDocument();
        travelDocument.setTravelPlan(travelPlan);
        travelDocument.setDocumentType(documentType);
        travelDocument.setEmployee(documentOwner);
        travelDocument.setUploadedBy(uploadedByEmployee);
        travelDocument.setOwnerType(isHr ? EOwnerType.HR : EOwnerType.EMPLOYEE);


        travelDocument.setUploadedAt(Instant.now());
        travelDocument.setFileName(file.getOriginalFilename());
        travelDocument.setFilePath(storedFileName);


        TravelDocument savedTravelDocument = travelDocumentRepository.save(travelDocument);
        return savedTravelDocument.getId();

    }
    private void validateFileFormat(MultipartFile file, DocumentType documentType) {
        String originalFilename = file.getOriginalFilename();

        if(originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file format");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();

        boolean allowed = Arrays.stream(documentType.getAllowedFormats().split(","))
                .map(String::trim)
                .anyMatch(s -> s.equalsIgnoreCase(extension));

        if(!allowed) {
            throw new IllegalArgumentException("Invalid file format");
        }

    }

    private void validateParticipant(Long travelPlanId, Long employeeId){
        boolean exists = employeeTravelRepository.existsByEmployeeIdAndTravelPlanId(employeeId, travelPlanId);
        if(!exists) {
            throw new AccessDeniedException("You are not assigned to this travel");
        }
    }

    private void validateFilePresence(MultipartFile file){
        if(file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Provided file is empty");
        }
    }
}
