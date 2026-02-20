package com.example.hrms.services.expense;

import com.example.hrms.dtos.expense.HrDecisionDto;
import com.example.hrms.dtos.expense.HrDecisionResponseDto;
import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Expense;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ExpenseStatus;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.ExpenseRepository;
import com.example.hrms.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class HrExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    //TODO Get filled expense method

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

        expense.setDecisionByEmployee(decisionByEmployee);

        expense.setStatus(ExpenseStatus.APPROVED);

        expense.setDecisionMadeAt(Instant.now());

        Expense savedExpense = expenseRepository.save(expense);
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
        return modelMapper.map(savedExpense, HrDecisionResponseDto.class);
    }


}
