package com.example.hrms.services.document;

import com.example.hrms.dtos.document.TravelDocumentResponseDto;
import com.example.hrms.dtos.document.UpdateTravelDocumentRequestDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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
                    travelDocumentResponseDto.setDocumentTypeId(travelDocument.getDocumentType().getId());

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
                    travelDocumentResponseDto.setDocumentTypeId(travelDocument.getDocumentType().getId());
                    return  travelDocumentResponseDto;
                })
                .toList();



    }

    public List<TravelDocumentResponseDto> findCommonDocumentByTravelPlanId(Long travelPlanId) {
        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVELPLAN FOUND"));

        List<TravelDocument> travelDocuments = travelDocumentRepository.findByTravelPlanIdAndEmployeeIdIsNull(travelPlanId);

        return travelDocuments.stream()
                .map(travelDocument -> {
                    TravelDocumentResponseDto  travelDocumentResponseDto = new TravelDocumentResponseDto();
                    travelDocumentResponseDto.setTravelDocumentId(travelDocument.getId());
                    travelDocumentResponseDto.setTravelPlanId(travelPlanId);
                    //travelDocumentResponseDto.setEmployeeId(employee.getId());
                    travelDocumentResponseDto.setDocumentTypeName(travelDocument.getDocumentType().getName());
                    travelDocumentResponseDto.setFileName(travelDocument.getFileName());
                    travelDocumentResponseDto.setUploadedByName(travelDocument.getUploadedBy().getFirstName() + " " + travelDocument.getUploadedBy().getLastName());
                    travelDocumentResponseDto.setUploadedByRole(travelDocument.getOwnerType());
                    travelDocumentResponseDto.setDocumentTypeId(travelDocument.getDocumentType().getId());

                    return  travelDocumentResponseDto;
                })
                .toList();
    }

    @Transactional
    public void deleteTravelDocument(Long travelDocumentId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("USER FOUND"));
        TravelDocument travelDocument = travelDocumentRepository.findById(travelDocumentId).orElseThrow(() -> new ResourceNotFoundException("NO DOCUMENT FOUND"));
        TravelPlan travelPlan = travelDocument.getTravelPlan();
        if(Boolean.FALSE.equals(travelPlan.getIsActive())){
            throw new IllegalStateException("Travel Plan has been deleted.");
        }
        validateDeletionTime(travelPlan);
        validateRoleBeforeDeletion(user, travelDocument);
        fileStorageService.delete("travel-documents", travelDocument.getFilePath());
        travelDocumentRepository.delete(travelDocument);

    }

    @Transactional
    public void updateTravelDocument(Long travelDocumentId, MultipartFile file, UpdateTravelDocumentRequestDto dto, Long userId) {

        // 1️⃣ Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER NOT FOUND"));

        // 2️⃣ Fetch document
        TravelDocument travelDocument = travelDocumentRepository.findById(travelDocumentId)
                .orElseThrow(() -> new ResourceNotFoundException("DOCUMENT NOT FOUND"));

        TravelPlan travelPlan = travelDocument.getTravelPlan();

        // 3️⃣ Validate travel plan status
        if (Boolean.FALSE.equals(travelPlan.getIsActive())) {
            throw new IllegalStateException("Travel Plan has been deleted.");
        }

        // 4️⃣ Validate business rules
        validateDeletionTime(travelPlan);
        validateRoleBeforeDeletion(user, travelDocument);

        // ✅ 5️⃣ FILE IS NOW OPTIONAL
        if (file != null && !file.isEmpty()) {

            String oldFilePath = travelDocument.getFilePath();

            // 6️⃣ Store new file
            String newFilePath = fileStorageService.store(file, "travel-documents");

            try {
                // 7️⃣ Update file fields
                travelDocument.setFileName(
                        Optional.ofNullable(file.getOriginalFilename()).orElse("unknown")
                );
                travelDocument.setFilePath(newFilePath);
                travelDocument.setUploadedAt(Instant.now());

                // 8️⃣ Delete old file safely
                if (oldFilePath != null) {
                    try {
                        fileStorageService.delete("travel-documents", oldFilePath);
                    } catch (Exception ex) {
                        log.warn("Failed to delete old travel document {}", oldFilePath, ex);
                    }
                }

            } catch (Exception e) {
                // rollback file if DB fails
                fileStorageService.delete("travel-documents", newFilePath);
                throw e;
            }
        }

        // ✅ 9️⃣ Update document type (optional)
        if (dto.getDocumentTypeId() != null) {
            DocumentType documentType = documentTypeRepository.findById(dto.getDocumentTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Document type not found"));

            travelDocument.setDocumentType(documentType);
        }

        // ✅ 🔥 SAVE AT END (important fix)
        travelDocumentRepository.save(travelDocument);
    }

//    @Transactional
//    public void updateTravelDocument(Long travelDocumentId, MultipartFile file, UpdateTravelDocumentRequestDto dto, Long userId) {
//
//        // 1️⃣ Fetch user
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("USER NOT FOUND"));
//
//        // 2️⃣ Fetch document
//        TravelDocument travelDocument = travelDocumentRepository.findById(travelDocumentId)
//                .orElseThrow(() -> new ResourceNotFoundException("DOCUMENT NOT FOUND"));
//
//        TravelPlan travelPlan = travelDocument.getTravelPlan();
//
//        // 3️⃣ Validate travel plan status
//        if (Boolean.FALSE.equals(travelPlan.getIsActive())) {
//            throw new IllegalStateException("Travel Plan has been deleted.");
//        }
//
//        // 4️⃣ Validate business rules
//        validateDeletionTime(travelPlan); // reuse if same logic applies
//        validateRoleBeforeDeletion(user, travelDocument);
//
//        // 5️⃣ Validate file
//        validateFilePresence(file);
//
//        String oldFilePath = travelDocument.getFilePath();
//
//        // 6️⃣ Store new file
//        String newFilePath = fileStorageService.store(file, "travel-documents");
//
//        try {
//            // 7️⃣ Update DB
//            travelDocument.setFileName(
//                    Optional.ofNullable(file.getOriginalFilename()).orElse("unknown")
//            );
//            travelDocument.setFilePath(newFilePath);
//            travelDocument.setUploadedAt(Instant.now());
//
//            travelDocumentRepository.save(travelDocument);
//
//        } catch (Exception e) {
//            // rollback file if DB fails
//            fileStorageService.delete("travel-documents", newFilePath);
//            throw e;
//        }
//
//        // 8️⃣ Delete old file safely
//        if (oldFilePath != null) {
//            try {
//                fileStorageService.delete("travel-documents", oldFilePath);
//            } catch (Exception ex) {
//                log.warn("Failed to delete old travel document {}", oldFilePath, ex);
//            }
//        }
//
//        // (optional)
//        if (dto.getDocumentTypeId() != null) {
//            DocumentType documentType = documentTypeRepository.findById(dto.getDocumentTypeId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Document type not found"));
//
//            travelDocument.setDocumentType(documentType);
//        }
//    }


    public void validateRoleBeforeDeletion(User user, TravelDocument travelDocument) {

        boolean isHr = user.getRoles().stream()
                .anyMatch(role -> ERole.ROLE_HR.equals(role.getName()));

        boolean isEmployee = user.getRoles().stream()
                .anyMatch(role -> ERole.ROLE_EMPLOYEE.equals(role.getName()));

        if (!isHr && !isEmployee) {
            throw new AccessDeniedException("You are not allowed to perform this action.");
        }

        EOwnerType ownerType = travelDocument.getOwnerType();

        if (isHr) {
            if (ownerType == EOwnerType.EMPLOYEE) {
                throw new AccessDeniedException("HR cannot delete employee documents.");
            }
            return;
        }

        if (isEmployee) {
            if (ownerType == EOwnerType.HR) {
                throw new AccessDeniedException("Employees cannot delete HR documents.");
            }

            if (user.getEmployee() == null || travelDocument.getEmployee() == null ||
                    !user.getEmployee().getId().equals(travelDocument.getEmployee().getId())) {
                throw new AccessDeniedException("You can only delete your own documents.");
            }
        }
    }
//    public void validateRoleBeforeDeletion(User user, TravelDocument travelDocument) {
//
//        boolean isHr = user.getRoles().stream()
//                .anyMatch(role -> ERole.ROLE_HR.equals(role.getName()));
//
//        boolean isEmployee = user.getRoles().stream()
//                .anyMatch(role -> ERole.ROLE_EMPLOYEE.equals(role.getName()));
//
//        if (!isHr && !isEmployee) {
//            throw new AccessDeniedException("You are not allowed to perform this action.");
//        }
//
//        EOwnerType ownerType = travelDocument.getOwnerType();
//
//        if (isHr && ownerType == EOwnerType.EMPLOYEE) {
//            throw new AccessDeniedException("HR cannot delete employee documents.");
//        }
//
//        if (isEmployee) {
//            if (ownerType == EOwnerType.HR) {
//                throw new AccessDeniedException("Employees cannot delete HR documents.");
//            }
//
//            if (user.getEmployee() == null || travelDocument.getEmployee() == null ||
//                    !user.getEmployee().getId().equals(travelDocument.getEmployee().getId())) {
//                throw new AccessDeniedException("You can only delete your own documents.");
//            }
//        }
//    }

    //Hr can delete common docs
    //Employee can delete only his docs
//    public void validateRoleBeforeDeletion(User user, TravelDocument travelDocument){
//        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
//        boolean isEmployee = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_EMPLOYEE);
//        EOwnerType ownerType = travelDocument.getOwnerType();
//        if(isHr){
//            if(ownerType == EOwnerType.EMPLOYEE){
//                throw new AccessDeniedException("You are not allowed to perform this action.");
//            }
//        }
//        if(isEmployee){
//            if(ownerType == EOwnerType.HR){
//                throw new AccessDeniedException("You are not allowed to perform this action.");
//            }
//            if(!user.getEmployee().getId().equals(travelDocument.getEmployee().getId())) {
//                throw new AccessDeniedException("You are not allowed to perform this action.");
//            }
//
//        }
//
//    }

    public void validateDeletionTime(TravelPlan travelPlan){
        LocalDate now = LocalDate.now();
        LocalDate startDate = travelPlan.getStartDate();

        if(!now.isBefore(startDate)){
            throw new IllegalStateException("Document cannot be deleted because travel plan has already been started.");
        }

    }
}
