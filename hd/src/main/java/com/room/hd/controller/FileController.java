package com.room.hd.controller;

import com.room.hd.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * File Upload Controller
 */
@RestController
@RequestMapping("/files")
@Slf4j
public class FileController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url:http://localhost:8080/uploads}")
    private String accessUrl;
    
    private final String absoluteUploadPath;
    
    /**
     * Constructor, initialize upload path
     */
    public FileController(@Value("${file.upload.path}") String uploadPath) {
        // Ensure using absolute path
        if (uploadPath.startsWith("src")) {
            // Use path outside project root directory
            this.absoluteUploadPath = "C:/Users/songj/Desktop/RestaurantReservation/uploads";
        } else {
            this.absoluteUploadPath = uploadPath;
        }
        
        // Create upload directory and subdirectories
        createDirectoryIfNotExists(this.absoluteUploadPath);
        createDirectoryIfNotExists(this.absoluteUploadPath + "/images");
        createDirectoryIfNotExists(this.absoluteUploadPath + "/videos");
        
        log.info("File upload path initialized to: {}", this.absoluteUploadPath);
    }
    
    /**
     * Create directory if it doesn't exist
     */
    private void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("Created directory: {}", dir.getAbsolutePath());
            } else {
                log.warn("Failed to create directory: {}", dir.getAbsolutePath());
            }
        }
    }

    /**
     * Image upload
     * @param file Image file
     * @return Image URL
     */
    @PostMapping("/images")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "images");
    }

    /**
     * Video upload
     * @param file Video file
     * @return Video URL
     */
    @PostMapping("/videos")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "videos");
    }

    /**
     * Common file upload method
     * @param file File to upload
     * @param fileType File type (images or videos)
     * @return File URL
     */
    private Result<String> uploadFile(MultipartFile file, String fileType) {
        try {
            if (file.isEmpty()) {
                return Result.error(500, "Upload file is empty");
            }
            
            // Get file extension
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error(500, "File name is empty");
            }
            
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // Generate unique filename
            String fileName = UUID.randomUUID().toString() + suffix;
            
            // Save file using absolute path
            String targetPath = absoluteUploadPath + "/" + fileType;
            File targetDir = new File(targetPath);
            if (!targetDir.exists()) {
                boolean created = targetDir.mkdirs();
                if (!created) {
                    log.error("Failed to create directory: {}", targetDir.getAbsolutePath());
                    return Result.error(500, "Failed to create directory");
                }
            }
            
            File dest = new File(targetDir, fileName);
            
            // Save file
            log.info("Saving file to: {}", dest.getAbsolutePath());
            file.transferTo(dest);
            
            // Generate access URL
            String url = accessUrl + "/" + fileType + "/" + fileName;
            
            log.info("File uploaded successfully: {}", url);
            
            return Result.success(url);
        } catch (Exception e) {
            log.error("File upload failed", e);
            return Result.error(500, "File upload failed: " + e.getMessage());
        }
    }
} 