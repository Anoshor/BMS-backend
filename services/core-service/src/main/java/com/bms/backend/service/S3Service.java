package com.bms.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.base-url}")
    private String baseUrl;

    @Value("${aws.cloudfront.domain:}")
    private String cloudFrontDomain;

    @Value("${aws.cloudfront.enabled:false}")
    private boolean cloudFrontEnabled;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public enum FileType {
        PROFILE("profile"),
        MAINTENANCE("maintenance"),
        PROPERTY("property"),
        DOCUMENT("documents"),
        OTHER("other");

        private final String folder;

        FileType(String folder) {
            this.folder = folder;
        }

        public String getFolder() {
            return folder;
        }
    }

    public String uploadFile(MultipartFile file, UUID userId, FileType fileType) throws IOException {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String key = generateS3Key(userId, fileType, fileName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return generateFileUrl(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    public InputStream downloadFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(getObjectRequest);
            return responseInputStream;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from S3: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    public List<String> listUserFiles(UUID userId, FileType fileType) {
        try {
            String prefix = String.format("users/%s/%s/", userId.toString(), fileType.getFolder());

            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

            return listObjectsV2Response.contents().stream()
                    .map(S3Object::key)
                    .map(this::generateFileUrl)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list files from S3: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("File content type cannot be determined");
        }

        boolean isValidType = ALLOWED_IMAGE_TYPES.contains(contentType) ||
                             ALLOWED_DOCUMENT_TYPES.contains(contentType);

        if (!isValidType) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: Images (JPEG, PNG, GIF, WebP) and Documents (PDF, DOC, DOCX)");
        }
    }

    private String generateFileName(String originalFilename) {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return timestamp + "_" + randomId + extension;
    }

    private String generateS3Key(UUID userId, FileType fileType, String fileName) {
        return String.format("users/%s/%s/%s", userId.toString(), fileType.getFolder(), fileName);
    }

    private String generateFileUrl(String key) {
        if (cloudFrontEnabled && cloudFrontDomain != null && !cloudFrontDomain.isEmpty()) {
            // Use CloudFront URL for faster loading
            return String.format("https://%s/%s", cloudFrontDomain, key);
        } else {
            // Fallback to direct S3 URL
            return String.format("%s/%s/%s", baseUrl, bucketName, key);
        }
    }

    private String extractKeyFromUrl(String fileUrl) {
        // Handle CloudFront URLs: https://d1234567890abc.cloudfront.net/users/uuid/folder/filename
        if (cloudFrontEnabled && cloudFrontDomain != null && fileUrl.contains(cloudFrontDomain)) {
            String domainPart = "https://" + cloudFrontDomain + "/";
            if (fileUrl.startsWith(domainPart)) {
                return fileUrl.substring(domainPart.length());
            }
        }

        // Handle S3 URLs: https://s3.us-east-2.amazonaws.com/bms-app-storage/users/uuid/folder/filename
        String bucketPart = "/" + bucketName + "/";
        int bucketIndex = fileUrl.indexOf(bucketPart);
        if (bucketIndex == -1) {
            throw new IllegalArgumentException("Invalid file URL format");
        }
        return fileUrl.substring(bucketIndex + bucketPart.length());
    }

    public boolean fileExists(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check file existence: " + e.getMessage(), e);
        }
    }
}