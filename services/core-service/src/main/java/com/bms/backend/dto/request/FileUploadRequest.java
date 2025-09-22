package com.bms.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FileUploadRequest {

    @NotNull(message = "File type is required")
    @NotBlank(message = "File type cannot be blank")
    private String fileType; // profile, maintenance, property, document, other

    private String description;
    private String category; // Additional categorization if needed

    public FileUploadRequest() {}

    public FileUploadRequest(String fileType, String description, String category) {
        this.fileType = fileType;
        this.description = description;
        this.category = category;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}