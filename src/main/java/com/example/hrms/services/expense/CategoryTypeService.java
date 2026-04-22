package com.example.hrms.services.expense;

import com.example.hrms.dtos.categoryType.CategoryTypeUpdateDto;
import com.example.hrms.dtos.categoryType.ExpenseCategoryCreateDto;
import com.example.hrms.dtos.expense.CategoryResponseDto;
import com.example.hrms.entities.CategoryType;
import com.example.hrms.entities.User;
import com.example.hrms.enums.ERole;
import com.example.hrms.enums.ExpenseCategory;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.CategoryTypeRepository;
import com.example.hrms.repositories.UserRepository;
import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryTypeService {
    private final CategoryTypeRepository categoryTypeRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<CategoryResponseDto> findAll(){
        return categoryTypeRepository.findByIsActiveTrue()
                .stream()
                .map(categoryType -> modelMapper.map(categoryType, CategoryResponseDto.class))
                .toList();
    }

    public CategoryResponseDto create(ExpenseCategoryCreateDto dto) {

        String name = dto.getName().trim();

        // 🔥 Find existing by name (IMPORTANT)
        CategoryType existing = categoryTypeRepository.findByNameIgnoreCase(name).orElse(null);

        if (existing != null) {

            if (existing.getDeletedAt() != null) {
                // ✅ RESTORE
                existing.setDeletedAt(null);
                existing.setIsActive(true);
                existing.setDeletedBy(null);

                CategoryType restored = categoryTypeRepository.save(existing);
                return modelMapper.map(restored, CategoryResponseDto.class);
            } else {
                // ❌ already active
                throw new RuntimeException("Expense category with this name already exists");
            }
        }

        // 🔹 create new
        CategoryType category = new CategoryType();
        category.setName(name);

        CategoryType saved = categoryTypeRepository.save(category);

        return modelMapper.map(saved, CategoryResponseDto.class);
    }

    public CategoryResponseDto update(Long id, CategoryTypeUpdateDto dto) {

        CategoryType category = categoryTypeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryType not found or already deleted"));

        String newName = dto.getName().trim();

        // 🔥 Check if another ACTIVE category has same name
        boolean exists = categoryTypeRepository.existsByNameIgnoreCase(newName)
                && !category.getName().equalsIgnoreCase(newName);

        if (exists) {
            throw new ResourceNotFoundException("CategoryType with this name already exists");
        }

        category.setName(newName);

        CategoryType updated = categoryTypeRepository.save(category);

        return modelMapper.map(updated, CategoryResponseDto.class);
    }

    public void delete(Long id, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isHr = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_HR);

        if (!isHr) {
            throw new AccessDeniedException("You are not allowed to perform this action");
        }

        CategoryType category = categoryTypeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("CategoryType not found or already deleted"));

        // 🔥 soft delete
        category.setIsActive(false);
        category.setDeletedAt(Instant.now());
        category.setDeletedBy(user.getEmployee());

        categoryTypeRepository.save(category);
    }
}
