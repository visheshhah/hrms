package com.example.hrms.services.expense;

import com.example.hrms.dtos.expense.CategoryResponseDto;
import com.example.hrms.entities.CategoryType;
import com.example.hrms.repositories.CategoryTypeRepository;
import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryTypeService {
    private final CategoryTypeRepository categoryTypeRepository;
    private final ModelMapper modelMapper;

    public List<CategoryResponseDto> findAll(){

        List<CategoryType> categoryTypes =  categoryTypeRepository.findAll();
        return categoryTypes.stream()
                .map(categoryType -> modelMapper.map(categoryType,CategoryResponseDto.class))
                .toList();
    }
}
