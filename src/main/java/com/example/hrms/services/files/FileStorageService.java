package com.example.hrms.services.files;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


public interface FileStorageService {
    String store(MultipartFile file, String folder);
    Resource load(String folder, String filename);
    void delete(String folder, String filename);
}
