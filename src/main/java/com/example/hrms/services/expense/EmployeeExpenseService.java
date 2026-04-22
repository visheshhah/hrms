package com.example.hrms.services.expense;

import com.example.hrms.dtos.expense.EmployeeExpenseResponseDto;
import com.example.hrms.dtos.expense.ExpenseProofDto;
import com.example.hrms.dtos.expense.SubmitExpenseDto;
import com.example.hrms.dtos.file.FileResponseDto;
import com.example.hrms.entities.*;
import com.example.hrms.enums.ERole;
import com.example.hrms.enums.ExpenseStatus;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import com.example.hrms.services.files.FileStorageService;
import com.example.hrms.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmployeeExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryTypeRepository categoryTypeRepository;
    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final FileStorageService fileStorageService;
    private final EmployeeTravelRepository employeeTravelRepository;
    private final ExpenseProffRepository expenseProffRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final NotificationService notificationService;
    private final HrExpenseService hrExpenseService;

    public FileResponseDto getExpenseProofFile(Long proofId) throws IOException {

        ExpenseProof proof = expenseProffRepository.findById(proofId)
                .orElseThrow(() -> new IllegalArgumentException("Proof not found"));

        Resource resource = fileStorageService
                .load("expense-proofs", proof.getFilePath());

        Path path = Paths.get("uploads/private/expense-proofs",
                proof.getFilePath());

        String detectedType = Files.probeContentType(path);

        String contentType = (detectedType != null)
                ? detectedType
                : "application/octet-stream";

        return new FileResponseDto(
                resource,
                proof.getFileName(),
                contentType
        );
    }

    public List<EmployeeExpenseResponseDto> findAllExpenses(Long userId, Long travelPlanId) {
        User uploader = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
        Employee employee = uploader.getEmployee();
        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndEmployeeId(travelPlanId, employee.getId());
//        return expenses.stream()
//                .map(expense -> {
//                    EmployeeExpenseResponseDto employeeExpenseResponseDto = new EmployeeExpenseResponseDto();
//                    employeeExpenseResponseDto.setId(expense.getId());
//                    employeeExpenseResponseDto.setTravelPlanId(travelPlanId);
//                    employeeExpenseResponseDto.set
//                })
        return expenses.stream()
                .map(this::mapToDto)
                .toList();


//        return expenses.stream()
//                .map(expense -> {
//                    EmployeeExpenseResponseDto employeeExpenseResponseDto = new EmployeeExpenseResponseDto();
//                    employeeExpenseResponseDto.setId(expense.getId());
//                    employeeExpenseResponseDto.setTravelPlanId(travelPlanId);
//                    employeeExpenseResponseDto.setRemark(expense.getRemark());
//                    employeeExpenseResponseDto.setCategoryName(expense.getCategory().getName());
//                    employeeExpenseResponseDto.setExpenseStatus(expense.getStatus());
//                    employeeExpenseResponseDto.setAmount(expense.getAmount());
//                    employeeExpenseResponseDto.setDescription(expense.getDescription());
//
//                    return employeeExpenseResponseDto;
//                })
//                .toList();
    }

    private EmployeeExpenseResponseDto mapToDto(Expense expense) {

        EmployeeExpenseResponseDto dto = new EmployeeExpenseResponseDto();

        dto.setId(expense.getId());
        dto.setTravelPlanId(expense.getTravelPlan().getId());
        dto.setRemark(expense.getRemark());
        dto.setCategoryName(expense.getCategory().getName());
        dto.setExpenseStatus(expense.getStatus());
        dto.setAmount(expense.getAmount());
        dto.setDescription(expense.getDescription());

        List<ExpenseProofDto> proofDtos = expense.getProofs()
                .stream()
                .map(proof -> {
                    ExpenseProofDto p = new ExpenseProofDto();
                    p.setId(proof.getId());
                    p.setFileName(proof.getFileName());
                    return p;
                })
                .toList();

        dto.setProofs(proofDtos);

        return dto;
    }

    public Long addExpense(SubmitExpenseDto submitExpenseDto, Long userId, Long travelPlanId, MultipartFile file) {

        validateFilePresence(file);
        User uploader = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
        Employee uploadedByEmployee = uploader.getEmployee();

        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));

        validateParticipant(travelPlanId, uploadedByEmployee.getId());

        CategoryType categoryType = categoryTypeRepository.findById(submitExpenseDto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("NO CATEGORY FOUND"));

        DocumentType receiptType = documentTypeRepository.findByCode("RECEIPT").orElseThrow(() -> new ResourceNotFoundException("Receipt document type missing"));

        validateExpenseSubmissionDate(travelPlan);
        validateFileFormat(file, receiptType);
        Expense expense = new Expense();
        expense.setAmount(submitExpenseDto.getAmount());
        expense.setEmployee(uploadedByEmployee);
        expense.setTravelPlan(travelPlan);
        expense.setCategory(categoryType);
        expense.setDescription(submitExpenseDto.getDescription());
        expense.setSubmittedAt(Instant.now());
        expense.setStatus(ExpenseStatus.SUBMITTED);

        Expense savedExpense = expenseRepository.save(expense);

        String storedFileName = fileStorageService.store(file, "expense-proofs");

        ExpenseProof expenseProof = new ExpenseProof();
        expenseProof.setExpense(savedExpense);
        expenseProof.setFileName(file.getOriginalFilename());
        expenseProof.setFilePath(storedFileName);
        expenseProof.setUploadedAt(Instant.now());
        expenseProof.setDocumentType(receiptType);

        expenseProffRepository.save(expenseProof);

        notificationService.notifyExpenseSubmitted(
                uploadedByEmployee,
                travelPlan.getCreatedByEmployee(),
                savedExpense,
                travelPlan
        );

        return savedExpense.getId();
    }

//    public BigDecimal getTotalClaimedAmount(Long travelPlanId, Long userId) {
//        User uploader = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
//        Employee employee = uploader.getEmployee();
//
//        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));
//        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndEmployeeId(travelPlanId, employee.getId());
//
//        return expenses.stream()
//                .map(Expense::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//
//    }

    private void validateFileFormat(MultipartFile file, DocumentType documentType) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file format");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toUpperCase();

        boolean allowed = Arrays.stream(documentType.getAllowedFormats().split(","))
                .map(String::trim)
                .anyMatch(s -> s.equalsIgnoreCase(extension));

        if (!allowed) {
            throw new IllegalArgumentException("Invalid file format");
        }

    }

    private void validateParticipant(Long travelPlanId, Long employeeId) {
        boolean exists = employeeTravelRepository.existsByEmployeeIdAndTravelPlanId(employeeId, travelPlanId);
        if (!exists) {
            throw new AccessDeniedException("You are not assigned to this travel");
        }
    }

    private void validateFilePresence(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Provided file is empty");
        }
    }

    //    Employee can add expenses only after trip start date and no later than
//      10 days after trip end date:
    private void validateExpenseSubmissionDate(TravelPlan travelPlan) {
        LocalDate startDate = travelPlan.getStartDate();
        LocalDate endDate = travelPlan.getEndDate().plusDays(10);
        LocalDate now = LocalDate.now();

        if (now.isBefore(startDate)) {
            throw new IllegalArgumentException("Cannot submit expense trip has not started yet");
        }

        if (now.isAfter(endDate)) {
            throw new IllegalArgumentException("Cannot submit expense as time limit has been reached");
        }

    }


    public BigDecimal getTotalClaimedAmountByTravelPlanAndEmployee(Long travelPlanId, Long userId) throws java.nio.file.AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));

        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));
        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndEmployeeIdAndStatusSubmitted(travelPlanId, user.getEmployee().getId());

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


    }

    public BigDecimal getTotalApprovedAmountByTravelPlanAndEmployee(Long travelPlanId, Long userId) throws java.nio.file.AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));

        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));
        List<Expense> expenses = expenseRepository.findApprovedExpenseByTravelPlanIdAndEmployeeIdAndStatusApproved(travelPlanId, user.getEmployee().getId());

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


    }

//    public void deleteExpense(Long expenseId){
//        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ResourceNotFoundException("EXPENSE NOT FOUND"));
////        if(!expense.getStatus().equals(ExpenseStatus.DRAFT)){
////            throw new IllegalStateException("Cannot delete expense");
////        }
//        validateExpenseStatus(expense);
//
//        //I have to delete expense record and proof associated with the record
//        //currently i have a list of proof which i added for future but currently
//        //there will be only one expense in the proofs
//        ExpenseProof expenseProof = expense.getProofs().get(0);
//        fileStorageService.delete("expense-proofs", expenseProof.getFilePath());
//        expenseProffRepository.delete(expenseProof);
//        expenseRepository.delete(expense);
//    }

@Transactional
public void deleteExpense(Long expenseId, Long userId) {

    User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
    Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new ResourceNotFoundException("EXPENSE NOT FOUND"));
    validateExpenseOwner(expense, user.getEmployee());

    validateDeletionTime(expense);
    validateExpenseStatus(expense);

    List<ExpenseProof> proofs = expense.getProofs();

    if (proofs != null && !proofs.isEmpty()) {
        for (ExpenseProof proof : proofs) {
            try {
                fileStorageService.delete("expense-proofs", proof.getFilePath());
            } catch (Exception e) {
                log.warn("Failed to delete file: " + proof.getFilePath());
            }
            expenseProffRepository.deleteAll(proofs);
        }
    }

    expenseRepository.delete(expense);
}

    @Transactional
    public void updateExpense(
            Long expenseId,
            SubmitExpenseDto dto,
            Long userId,
            MultipartFile file
    ) {

        // 1️⃣ Fetch expense
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("EXPENSE NOT FOUND"));

        // 2️⃣ Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));

        Employee employee = user.getEmployee();
        validateExpenseOwner(expense, user.getEmployee());

        // 3️⃣ Validate ownership / participant
        validateParticipant(expense.getTravelPlan().getId(), employee.getId());

        // 4️⃣ Validate business rules
        validateDeletionTime(expense); // reuse
        validateExpenseStatus(expense);

        // 5️⃣ Fetch category
        CategoryType categoryType = categoryTypeRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("NO CATEGORY FOUND"));

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description required");
        }

        // 6️⃣ Update basic fields
        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setCategory(categoryType);

        // 7️⃣ Handle file (optional)
        if (file != null && !file.isEmpty()) {

            DocumentType receiptType = documentTypeRepository.findByCode("RECEIPT")
                    .orElseThrow(() -> new ResourceNotFoundException("Receipt document type missing"));

            validateFileFormat(file, receiptType);

            // get existing proof (assuming one)
            ExpenseProof proof = expense.getProofs().stream()
                    .findFirst()
                    .orElse(null);

            String newFilePath = fileStorageService.store(file, "expense-proofs");

            try {
                if (proof != null) {

                    String oldFilePath = proof.getFilePath();

                    proof.setFileName(file.getOriginalFilename());
                    proof.setFilePath(newFilePath);
                    proof.setUploadedAt(Instant.now());
                    proof.setDocumentType(receiptType);

                    expenseProffRepository.save(proof);

                    // delete old file
                    if (oldFilePath != null) {
                        try {
                            fileStorageService.delete("expense-proofs", oldFilePath);
                        } catch (Exception ex) {
                            log.warn("Failed to delete old file: {}", oldFilePath, ex);
                        }
                    }

                } else {
                    // no existing proof → create new
                    ExpenseProof newProof = new ExpenseProof();
                    newProof.setExpense(expense);
                    newProof.setFileName(file.getOriginalFilename());
                    newProof.setFilePath(newFilePath);
                    newProof.setUploadedAt(Instant.now());
                    newProof.setDocumentType(receiptType);

                    expenseProffRepository.save(newProof);
                }

            } catch (Exception e) {
                fileStorageService.delete("expense-proofs", newFilePath);
                throw e;
            }
        }

        // 8️⃣ Save expense
        expenseRepository.save(expense);
    }

    public List<EmployeeExpenseResponseDto> findAllExpenseByStatus(Long employeeId, Long travelPlanId, String status) {
       return hrExpenseService.findAllExpenseByStatus(employeeId, travelPlanId, status);
    }

    private void validateDeletionTime(Expense expense) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = expense.getTravelPlan().getStartDate();
        LocalDate endDate = expense.getTravelPlan().getEndDate().plusDays(10);

        if (now.isBefore(startDate) || now.isAfter(endDate)) {
            throw new AccessDeniedException(
                    "You can only delete expenses between travel start date and 10 days after end date."
            );
        }
    }

    private void validateExpenseOwner(Expense expense, Employee employee) {
        if(!expense.getEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("You are not allowed to perform this operation");
        }
    }

    private void validateExpenseStatus(Expense expense) {
        ExpenseStatus status = expense.getStatus();

        if (status == ExpenseStatus.REJECTED || status == ExpenseStatus.APPROVED) {
            throw new IllegalStateException("Cannot delete expense");
        }
    }
}

//    private Employee getLoggedInEmployee() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//
//        if(authentication == null || !authentication.isAuthenticated()) {
//            throw new AccessDeniedException("You are not logged in");
//        }
//        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
//
//        User uploader = userRepository.findById(myUserDetails.getId()).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
//        return uploader.getEmployee();
//
//
//
//    }

