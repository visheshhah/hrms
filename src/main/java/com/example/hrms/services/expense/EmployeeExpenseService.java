package com.example.hrms.services.expense;

import com.example.hrms.dtos.expense.EmployeeExpenseResponseDto;
import com.example.hrms.dtos.expense.ExpenseProofDto;
import com.example.hrms.dtos.expense.SubmitExpenseDto;
import com.example.hrms.dtos.file.FileResponseDto;
import com.example.hrms.entities.*;
import com.example.hrms.enums.ExpenseStatus;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import com.example.hrms.services.files.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryTypeRepository categoryTypeRepository;
    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final FileStorageService fileStorageService;
    private final EmployeeTravelRepository employeeTravelRepository;
    private final ExpenseProffRepository expenseProffRepository;
    private final DocumentTypeRepository documentTypeRepository;


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

    public List<EmployeeExpenseResponseDto>  findAllExpenses(Long userId, Long travelPlanId) {
        User uploader = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
        Employee employee = uploader.getEmployee();
        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndEmployeeId(travelPlanId, employee.getId());

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

        return savedExpense.getId();
    }

    public BigDecimal getTotalClaimedAmount(Long travelPlanId, Long userId) {
        User uploader = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
        Employee employee = uploader.getEmployee();

        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));
        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndEmployeeId(travelPlanId, employee.getId());

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


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
}
