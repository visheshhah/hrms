package com.example.hrms.services.expense;

import com.example.hrms.dtos.expense.*;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Expense;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import com.example.hrms.enums.ExpenseStatus;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.ExpenseRepository;
import com.example.hrms.repositories.TravelPlanRepository;
import com.example.hrms.repositories.UserRepository;
import com.example.hrms.services.notification.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HrExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final TravelPlanRepository travelPlanRepository;

//    public HrDecisionResponseDto makeDecision(HrDecisionDto hrDecisionDto, Long hrId, String decision ) {
//        Expense expense = expenseRepository.findById(hrDecisionDto.getExpenseId()).orElseThrow(() ->  new ResourceNotFoundException("EXPENSE NOT FOUND"));
//        if(decision.equals("REJECTED")) {
//            if(hrDecisionDto.getRemark().isEmpty()){
//                throw new IllegalArgumentException("Please enter a valid remark");
//            }
//            expense.setRemark(hrDecisionDto.getRemark());
//            expense.setStatus(ExpenseStatus.REJECTED);
//        }
//
//
//        User creator = userRepository.findById(hrId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        Employee decisionByEmployee = creator.getEmployee();
//
//        expense.setDecisionByEmployee(decisionByEmployee);
//
//        expense.setStatus(ExpenseStatus.APPROVED);
//
//        expense.setDecisionMadeAt(Instant.now());
//
//        Expense savedExpense = expenseRepository.save(expense);
//        return modelMapper.map(savedExpense, HrDecisionResponseDto.class);
//    }

    public HrDecisionResponseDto approveExpense(HrDecisionDto hrDecisionDto, Long hrId) {
        Expense expense = expenseRepository.findById(hrDecisionDto.getExpenseId()).orElseThrow(() ->  new ResourceNotFoundException("EXPENSE NOT FOUND"));

        User creator = userRepository.findById(hrId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee decisionByEmployee = creator.getEmployee();

        expense.setRemark(hrDecisionDto.getRemark());
        expense.setDecisionByEmployee(decisionByEmployee);

        expense.setStatus(ExpenseStatus.APPROVED);

        expense.setDecisionMadeAt(Instant.now());

        Expense savedExpense = expenseRepository.save(expense);
        Employee employee = savedExpense.getEmployee();

        notificationService.notifyExpenseApproved(
                decisionByEmployee,
                employee,
                savedExpense
        );

        return modelMapper.map(savedExpense, HrDecisionResponseDto.class);
    }

    public HrDecisionResponseDto rejectExpense(HrDecisionDto hrDecisionDto, Long hrId) {
        Expense expense = expenseRepository.findById(hrDecisionDto.getExpenseId()).orElseThrow(() ->  new ResourceNotFoundException("EXPENSE NOT FOUND"));
        if(hrDecisionDto.getRemark().isEmpty()){
            throw new IllegalArgumentException("Please enter a valid remark");
        }

        expense.setRemark(hrDecisionDto.getRemark());
        expense.setStatus(ExpenseStatus.REJECTED);

        User creator = userRepository.findById(hrId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee decisionByEmployee = creator.getEmployee();

        expense.setDecisionByEmployee(decisionByEmployee);

        expense.setDecisionMadeAt(Instant.now());

        Expense savedExpense = expenseRepository.save(expense);
        Employee employee = savedExpense.getEmployee();

        notificationService.notifyExpenseRejected(
                decisionByEmployee,
                employee,
                savedExpense
        );
        return modelMapper.map(savedExpense, HrDecisionResponseDto.class);
    }

    public List<EmployeeExpenseResponseDto> findAllExpenses(Long employeeId, Long travelPlanId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() ->  new ResourceNotFoundException("Employee not found"));
        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndEmployeeId(travelPlanId, employee.getId());

        return expenses.stream()
                .map(this::mapToDto)
                .toList();
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

    public EmployeeExpenseResponseDto findExpenseDetails(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() ->  new ResourceNotFoundException("Expense not found"));
        return mapToDto(expense);
    }

    public BigDecimal getTotalClaimedAmountByTravelPlan(Long travelPlanId, Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }

        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));
        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndStatusSubmitted(travelPlanId);

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


    }

    public BigDecimal getTotalClaimedAmountByTravelPlanAndEmployee(Long travelPlanId, Long employeeId, Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
        employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("NO EMPLOYEE FOUND"));

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }

        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));
        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndEmployeeIdAndStatusSubmitted(travelPlanId, employeeId);

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


    }

    public BigDecimal getTotalApprovedAmountByTravelPlan(Long travelPlanId, Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }

        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));
        List<Expense> expenses = expenseRepository.findApprovedExpenseByTravelPlanIdAndStatusApproved(travelPlanId);

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


    }

    public BigDecimal getTotalApprovedAmountByTravelPlanAndEmployee(Long travelPlanId, Long employeeId, Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("NO USER FOUND"));
        employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException("NO EMPLOYEE FOUND"));

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);
        if(!isHr) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }

        travelPlanRepository.findById(travelPlanId).orElseThrow(() -> new ResourceNotFoundException("NO TRAVEL PLAN FOUND"));
        List<Expense> expenses = expenseRepository.findApprovedExpenseByTravelPlanIdAndEmployeeIdAndStatusApproved(travelPlanId, employeeId);

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


    }

    public List<EmployeeExpenseResponseDto> findAllExpenseByStatus(Long employeeId, Long travelPlanId, String status) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() ->  new ResourceNotFoundException("Employee not found"));

        ExpenseStatus expenseStatus;
        String expenseStatusName = status.toLowerCase();
        switch (expenseStatusName) {
            case "approved":
                expenseStatus = ExpenseStatus.APPROVED;
                break;
            case "rejected":
                expenseStatus = ExpenseStatus.REJECTED;
                break;
            default:
                expenseStatus = ExpenseStatus.SUBMITTED;
                break;
        }

        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndEmployeeIdAndStatus(travelPlanId, employee.getId(), expenseStatus);

        return expenses.stream()
                .map(this::mapToDto)
                .toList();
    }

    public HrDecisionResponseDto approveExpense(ExpenseDecisionDto expenseDecisionDto, Long hrId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ResourceNotFoundException("EXPENSE NOT FOUND"));

        User creator = userRepository.findById(hrId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee decisionByEmployee = creator.getEmployee();

        //
        ExpenseStatus expenseStatus;
        String expenseStatusName = expenseDecisionDto.getStatus().toString();
        switch (expenseStatusName) {
            case "APPROVED":
                expenseStatus = ExpenseStatus.APPROVED;
                break;
            case "REJECTED":
                expenseStatus = ExpenseStatus.REJECTED;
                break;
            default:
                throw new IllegalStateException("Invalid expense status");

        }
        //

        expense.setDecisionByEmployee(decisionByEmployee);

        expense.setStatus(expenseStatus);

        expense.setDecisionMadeAt(Instant.now());

        Expense savedExpense = expenseRepository.save(expense);
        Employee employee = savedExpense.getEmployee();

        notificationService.notifyExpenseApproved(
                decisionByEmployee,
                employee,
                savedExpense
        );

        return modelMapper.map(savedExpense, HrDecisionResponseDto.class);

    }
}
