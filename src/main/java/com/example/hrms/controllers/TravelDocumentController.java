package com.example.hrms.controllers;

import com.example.hrms.dtos.document.UploadTravelDocumentRequestDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.document.TravelDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
@RequestMapping("/document")
public class TravelDocumentController {

    private final TravelDocumentService travelDocumentService;

    @PostMapping(value = "/{travel-plan-id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> uploadDocument(@PathVariable("travel-plan-id") Long travelPlanId, @RequestPart("file") MultipartFile file, @RequestPart("data") UploadTravelDocumentRequestDto uploadTravelDocumentRequestDto, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(travelDocumentService.uploadDocument(travelPlanId, uploadTravelDocumentRequestDto, file, userId));

    }
}
