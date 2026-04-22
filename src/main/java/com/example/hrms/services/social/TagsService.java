package com.example.hrms.services.social;

import com.example.hrms.dtos.social.TagsTypeDto;
import com.example.hrms.dtos.tag.TagRequestDto;
import com.example.hrms.entities.Tags;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.TagsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagsService {
    private final TagsRepository tagsRepository;
    private final ModelMapper modelMapper;

    public TagsTypeDto create(TagRequestDto dto) {

        Optional<Tags> existing = tagsRepository.findByTagName(dto.getTagName());

        if (existing.isPresent()) {
            Tags tag = existing.get();

            if (!tag.getIsActive()) {
                tag.setIsActive(true);
                return modelMapper.map(tagsRepository.save(tag), TagsTypeDto.class);
            }

            throw new RuntimeException("Tag already exists");
        }

        Tags tag = new Tags();
        tag.setTagName(dto.getTagName());

        return modelMapper.map(tagsRepository.save(tag), TagsTypeDto.class);
    }

    public List<TagsTypeDto> findAll() {
        return tagsRepository.findByIsActiveTrue()
                .stream()
                .map(tag -> modelMapper.map(tag, TagsTypeDto.class))
                .toList();
    }

    public TagsTypeDto findById(Long id) {
        Tags tag = tagsRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        return modelMapper.map(tag, TagsTypeDto.class);
    }

    public TagsTypeDto update(Long id, TagRequestDto dto) {

        Tags tag = tagsRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        Optional<Tags> existing = tagsRepository.findByTagName(dto.getTagName());

        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new RuntimeException("Tag name already in use");
        }

        tag.setTagName(dto.getTagName());

        return modelMapper.map(tagsRepository.save(tag), TagsTypeDto.class);
    }

    public void delete(Long id) {

        Tags tag = tagsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (!tag.getIsActive()) {
            throw new RuntimeException("Tag already deleted");
        }

        tag.setIsActive(false);
        tagsRepository.save(tag);
    }
}
