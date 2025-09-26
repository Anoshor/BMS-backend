package com.bms.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class S3ServiceCloudFrontTest {

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service();
        ReflectionTestUtils.setField(s3Service, "bucketName", "bms-app-storage");
        ReflectionTestUtils.setField(s3Service, "baseUrl", "https://s3.us-east-2.amazonaws.com");
    }

    @Test
    void testGenerateFileUrl_WithCloudFrontDisabled() throws Exception {
        // Setup
        ReflectionTestUtils.setField(s3Service, "cloudFrontEnabled", false);
        ReflectionTestUtils.setField(s3Service, "cloudFrontDomain", "");

        // Call private method via reflection
        String key = "users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg";
        String result = (String) ReflectionTestUtils.invokeMethod(s3Service, "generateFileUrl", key);

        // Assert S3 URL format
        assertEquals("https://s3.us-east-2.amazonaws.com/bms-app-storage/users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg", result);
    }

    @Test
    void testGenerateFileUrl_WithCloudFrontEnabled() throws Exception {
        // Setup
        ReflectionTestUtils.setField(s3Service, "cloudFrontEnabled", true);
        ReflectionTestUtils.setField(s3Service, "cloudFrontDomain", "d1234567890abc.cloudfront.net");

        // Call private method via reflection
        String key = "users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg";
        String result = (String) ReflectionTestUtils.invokeMethod(s3Service, "generateFileUrl", key);

        // Assert CloudFront URL format
        assertEquals("https://d1234567890abc.cloudfront.net/users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg", result);
    }

    @Test
    void testExtractKeyFromUrl_S3Url() throws Exception {
        // Setup
        ReflectionTestUtils.setField(s3Service, "cloudFrontEnabled", false);
        ReflectionTestUtils.setField(s3Service, "cloudFrontDomain", "");

        String s3Url = "https://s3.us-east-2.amazonaws.com/bms-app-storage/users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg";
        String result = (String) ReflectionTestUtils.invokeMethod(s3Service, "extractKeyFromUrl", s3Url);

        assertEquals("users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg", result);
    }

    @Test
    void testExtractKeyFromUrl_CloudFrontUrl() throws Exception {
        // Setup
        ReflectionTestUtils.setField(s3Service, "cloudFrontEnabled", true);
        ReflectionTestUtils.setField(s3Service, "cloudFrontDomain", "d1234567890abc.cloudfront.net");

        String cloudFrontUrl = "https://d1234567890abc.cloudfront.net/users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg";
        String result = (String) ReflectionTestUtils.invokeMethod(s3Service, "extractKeyFromUrl", cloudFrontUrl);

        assertEquals("users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg", result);
    }

    @Test
    void testGenerateS3Key() throws Exception {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        S3Service.FileType fileType = S3Service.FileType.PROFILE;
        String fileName = "image.jpg";

        String result = (String) ReflectionTestUtils.invokeMethod(s3Service, "generateS3Key", userId, fileType, fileName);

        assertEquals("users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg", result);
    }

    @Test
    void testCloudFrontFallback_EmptyDomain() throws Exception {
        // Setup - enabled but empty domain should fallback to S3
        ReflectionTestUtils.setField(s3Service, "cloudFrontEnabled", true);
        ReflectionTestUtils.setField(s3Service, "cloudFrontDomain", "");

        String key = "users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg";
        String result = (String) ReflectionTestUtils.invokeMethod(s3Service, "generateFileUrl", key);

        // Should fallback to S3 URL
        assertEquals("https://s3.us-east-2.amazonaws.com/bms-app-storage/users/123e4567-e89b-12d3-a456-426614174000/profile/image.jpg", result);
    }
}