package com.example.hrms.services.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.upload-dir}")
    private String baseUploadDir;

    @Override
    public String store(MultipartFile file, String folder) {
        try {
            if(file.isEmpty()) {
                throw new IllegalArgumentException("Cannot upload a null or empty file!");
            }

            Path uploadPath = Paths.get(baseUploadDir, folder).toAbsolutePath().normalize();

            Files.createDirectories(uploadPath);

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            if(originalFilename.contains("..")) {
                throw new IllegalArgumentException("Invalid file");
            }

            String extension = "";
            if(originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID() + extension;
            Path targetLocation = uploadPath.resolve(uniqueFilename);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFilename;
        }catch(IOException e){
            throw new RuntimeException("Could not store file " + file.getOriginalFilename() + "!", e);
        }
    }

    @Override
    public Resource load(String folder, String filename) {
        try {
         Path filePath = Paths.get(baseUploadDir, folder).toAbsolutePath().normalize().resolve(filename);

         Resource resource = new UrlResource(filePath.toUri());

         if(!resource.exists()){
             throw new RuntimeException("Resource not found: " + filePath);
         }

         return resource;
        }catch(MalformedURLException e) {
            throw new RuntimeException("Could not load file " + filename, e);
        }
    }

    @Override
    public void delete(String folder, String filename) {
        try {
            Path filePath = Paths.get(baseUploadDir, folder).toAbsolutePath().normalize().resolve(filename);
            Files.deleteIfExists(filePath);

        }catch (IOException e){
            throw new RuntimeException("Could not delete file " + filename, e);
        }

    }
}
