package com.bms.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ApiResponse<T> {
    
    private boolean success;
    private T data;
    private String message;
    private List<String> errors;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String path;
    
    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, T data, String message) {
        this();
        this.success = success;
        this.data = data;
        this.message = message;
    }
    
    // Static factory methods
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
    
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        ApiResponse<T> response = new ApiResponse<>(false, null, message);
        response.setErrors(errors);
        return response;
    }
    
    // Builder pattern
    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }
    
    public static class ApiResponseBuilder<T> {
        private boolean success;
        private T data;
        private String message;
        private List<String> errors;
        private String path;
        
        public ApiResponseBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }
        
        public ApiResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }
        
        public ApiResponseBuilder<T> message(String message) {
            this.message = message;
            return this;
        }
        
        public ApiResponseBuilder<T> errors(List<String> errors) {
            this.errors = errors;
            return this;
        }
        
        public ApiResponseBuilder<T> path(String path) {
            this.path = path;
            return this;
        }
        
        public ApiResponse<T> build() {
            ApiResponse<T> response = new ApiResponse<>(success, data, message);
            response.setErrors(errors);
            response.setPath(path);
            return response;
        }
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}