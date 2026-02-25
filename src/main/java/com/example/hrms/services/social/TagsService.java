package com.example.hrms.services.social;

import com.example.hrms.dtos.social.TagsTypeDto;
import com.example.hrms.entities.Tags;
import com.example.hrms.repositories.TagsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagsService {
    private final TagsRepository tagsRepository;
    private final ModelMapper modelMapper;

    public List<TagsTypeDto> findAll() {
        List<Tags>  tags = tagsRepository.findAll();
        return tags.stream()
                .map(tag -> modelMapper.map(tag, TagsTypeDto.class))
                .toList();
    }
}
