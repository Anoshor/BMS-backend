package com.bms.backend.controller;

import com.bms.backend.dto.request.FileUploadRequest;
import com.bms.backend.dto.response.ApiResponse;
import com.bms.backend.dto.response.FileUploadResponse;
import com.bms.backend.entity.User;
import com.bms.backend.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadRequest") String uploadRequestJson) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            // Parse the upload request
            FileUploadRequest uploadRequest = objectMapper.readValue(uploadRequestJson, FileUploadRequest.class);

            // Convert string to enum
            S3Service.FileType fileType;
            try {
                fileType = S3Service.FileType.valueOf(uploadRequest.getFileType().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, null, "Invalid file type. Allowed: profile, maintenance, property, document, other"));
            }

            // Upload file
            String fileUrl = s3Service.uploadFile(file, user.getId(), fileType);

            // Create response
            FileUploadResponse response = new FileUploadResponse(
                    fileUrl,
                    file.getOriginalFilename(),
                    uploadRequest.getFileType(),
                    file.getSize(),
                    file.getContentType(),
                    user.getId(),
                    uploadRequest.getDescription(),
                    uploadRequest.getCategory()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, response, "File uploaded successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/image")
    public ResponseEntity<?> downloadImage(@RequestParam String fileUrl) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            // Validate that the file exists
            if (!s3Service.fileExists(fileUrl)) {
                return ResponseEntity.notFound().build();
            }

            // Download file
            InputStream fileStream = s3Service.downloadFile(fileUrl);

            // Determine content type based on file extension
            String contentType = "application/octet-stream";
            if (fileUrl.toLowerCase().contains(".jpg") || fileUrl.toLowerCase().contains(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileUrl.toLowerCase().contains(".png")) {
                contentType = "image/png";
            } else if (fileUrl.toLowerCase().contains(".gif")) {
                contentType = "image/gif";
            } else if (fileUrl.toLowerCase().contains(".webp")) {
                contentType = "image/webp";
            } else if (fileUrl.toLowerCase().contains(".pdf")) {
                contentType = "application/pdf";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("max-age=3600"); // Cache for 1 hour

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to download file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteFile(@RequestParam String fileUrl) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            // TODO: Add authorization check to ensure user can delete this file

            s3Service.deleteFile(fileUrl);

            return ResponseEntity.ok(new ApiResponse<>(true, "File deleted successfully", "File deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to delete file: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<String>>> listUserFiles(
            @RequestParam String fileType) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            S3Service.FileType type;
            try {
                type = S3Service.FileType.valueOf(fileType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, null, "Invalid file type. Allowed: profile, maintenance, property, document, other"));
            }

            List<String> files = s3Service.listUserFiles(user.getId(), type);

            return ResponseEntity.ok(new ApiResponse<>(true, files, "Files listed successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, null, "Failed to list files: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(new ApiResponse<>(true, "File service is running", "File service is healthy"));
    }
}