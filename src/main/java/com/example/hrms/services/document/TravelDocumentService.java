package com.example.hrms.services.document;

import com.example.hrms.dtos.document.TravelDocumentResponseDto;
import com.example.hrms.dtos.document.UploadTravelDocumentRequestDto;
import com.example.hrms.dtos.expense.EmployeeExpenseResponseDto;
import com.example.hrms.dtos.file.FileResponseDto;
import com.example.hrms.entities.*;
import com.example.hrms.enums.EOwnerType;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import com.example.hrms.services.files.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

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

    public List<TravelDocumentResponseDto> findAllDocuments(Long userId, Long travelPlanId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
        Employee employee = user.getEmployee();

        List<TravelDocument> travelDocuments = travelDocumentRepository.findByTravelPlanIdAndEmployeeId(travelPlanId, employee.getId());

        return travelDocuments.stream()
                .map(travelDocument -> {
                    TravelDocumentResponseDto  travelDocumentResponseDto = new TravelDocumentResponseDto();
                    travelDocumentResponseDto.setTravelDocumentId(travelDocument.getId());
                    travelDocumentResponseDto.setTravelPlanId(travelPlanId);
                    travelDocumentResponseDto.setEmployeeId(employee.getId());
                    travelDocumentResponseDto.setDocumentTypeName(travelDocument.getDocumentType().getName());
                    travelDocumentResponseDto.setFileName(travelDocument.getFileName());
                    travelDocumentResponseDto.setUploadedByName(travelDocument.getUploadedBy().getFirstName() + " " + travelDocument.getUploadedBy().getLastName());
                    travelDocumentResponseDto.setUploadedByRole(travelDocument.getOwnerType());

                    return  travelDocumentResponseDto;
                })
                .toList();



    }

    public FileResponseDto getDocumentFile(Long travelDocumentId) throws IOException {
        TravelDocument travelDocument= travelDocumentRepository.findById(travelDocumentId).orElseThrow(() -> new ResourceNotFoundException("NO DOCUMENT FOUND"));

        Resource resource = fileStorageService
                .load("travel-documents", travelDocument.getFilePath());

        Path path = Paths.get("uploads/private/travel-documents",
                travelDocument.getFilePath());

        String detectedType = Files.probeContentType(path);

        String contentType = (detectedType != null)
                ? detectedType
                : "application/octet-stream";

        return new FileResponseDto(
                resource,
                travelDocument.getFileName(),
                contentType
        );

    }

    public List<TravelDocumentResponseDto> findAllDocumentsByEmployeeId(Long employeeId, Long travelPlanId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("NO EMPLOYEE FOUND"));

        List<TravelDocument> travelDocuments = travelDocumentRepository.findByTravelPlanIdAndEmployeeId(travelPlanId, employee.getId());

        return travelDocuments.stream()
                .map(travelDocument -> {
                    TravelDocumentResponseDto  travelDocumentResponseDto = new TravelDocumentResponseDto();
                    travelDocumentResponseDto.setTravelDocumentId(travelDocument.getId());
                    travelDocumentResponseDto.setTravelPlanId(travelPlanId);
                    travelDocumentResponseDto.setEmployeeId(employee.getId());
                    travelDocumentResponseDto.setDocumentTypeName(travelDocument.getDocumentType().getName());
                    travelDocumentResponseDto.setFileName(travelDocument.getFileName());
                    travelDocumentResponseDto.setUploadedByName(travelDocument.getUploadedBy().getFirstName() + " " + travelDocument.getUploadedBy().getLastName());
                    travelDocumentResponseDto.setUploadedByRole(travelDocument.getOwnerType());

                    return  travelDocumentResponseDto;
                })
                .toList();



    }
}
