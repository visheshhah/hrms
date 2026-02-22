package com.example.hrms.controllers;

import com.example.hrms.dtos.document.TravelDocumentResponseDto;
import com.example.hrms.dtos.document.UploadTravelDocumentRequestDto;
import com.example.hrms.dtos.expense.EmployeeExpenseResponseDto;
import com.example.hrms.dtos.file.FileResponseDto;
import com.example.hrms.entities.MyUserDetails;
import com.example.hrms.services.document.TravelDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    @GetMapping("employee/{travel-plan-id}")
    public ResponseEntity<List<TravelDocumentResponseDto>> getEmployeeDocuments(@PathVariable("travel-plan-id") Long travelPlanId, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long employeeId = userDetails.getId();
        List<TravelDocumentResponseDto> employeeDocuments = travelDocumentService.findAllDocuments(employeeId, travelPlanId);
        return ResponseEntity.ok(employeeDocuments);
    }

    @GetMapping("employee/document-files/{id}/view")
    public ResponseEntity<Resource> viewDocuments(@PathVariable Long id) throws IOException {

        FileResponseDto file = travelDocumentService.getDocumentFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getResource());
    }

    @GetMapping("employee/{employee-id}/{travel-plan-id}")
    public ResponseEntity<List<TravelDocumentResponseDto>> getEmployeeDocumentsById(@PathVariable("travel-plan-id") Long travelPlanId, @PathVariable("employee-id") Long employeeId) {
        List<TravelDocumentResponseDto> employeeDocuments = travelDocumentService.findAllDocumentsByEmployeeId(employeeId, travelPlanId);
        return ResponseEntity.ok(employeeDocuments);
    }
}
