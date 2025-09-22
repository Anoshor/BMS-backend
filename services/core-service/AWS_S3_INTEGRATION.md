# AWS S3 Integration for BMS Backend

## Overview
This document outlines the AWS S3 integration for the Building Management System (BMS) backend, enabling secure file storage and retrieval with organized folder structures.

## Architecture

### Folder Structure
```
bms-app-storage/
└── users/
    └── {user-uuid}/
        ├── profile/          # Profile pictures
        ├── maintenance/      # Maintenance request photos
        ├── property/         # Property images
        ├── documents/        # Documents (PDF, DOC, etc.)
        └── other/           # Other files
```

### Components
- **S3Config.java** - AWS S3 client configuration
- **S3Service.java** - File operations service
- **FileController.java** - REST API endpoints
- **DTOs** - Request/Response models

## API Endpoints

### 1. File Upload
**POST** `/api/v1/files/upload`

**Headers:**
```
Authorization: Bearer {jwt-token}
Content-Type: multipart/form-data
```

**Parameters:**
- `file` (multipart file) - The file to upload
- `uploadRequest` (JSON string) - Upload metadata

**Upload Request JSON:**
```json
{
  "fileType": "profile",
  "description": "Profile picture",
  "category": "user"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "fileUrl": "https://s3.us-east-2.amazonaws.com/bms-app-storage/users/{uuid}/profile/{filename}",
    "fileName": "profile.jpg",
    "fileType": "profile",
    "fileSize": 2048576,
    "contentType": "image/jpeg",
    "userId": "uuid",
    "uploadedAt": "2025-01-21T10:30:00Z"
  },
  "message": "File uploaded successfully"
}
```

**cURL Example:**
```bash
curl -X POST "http://localhost:8080/api/v1/files/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F 'uploadRequest={"fileType":"profile","description":"Profile picture"}'
```

### 2. Image Download
**GET** `/api/v1/files/image?fileUrl={encoded-url}`

**Headers:**
```
Authorization: Bearer {jwt-token}
```

**Response:** Binary image data with appropriate Content-Type

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/files/image?fileUrl=https%3A%2F%2Fs3.us-east-2.amazonaws.com%2Fbms-app-storage%2Fusers%2F{uuid}%2Fprofile%2Fimage.jpg" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output downloaded_image.jpg
```

### 3. List User Files
**GET** `/api/v1/files/list?fileType={type}`

**Parameters:**
- `fileType` - One of: profile, maintenance, property, document, other

**Response:**
```json
{
  "success": true,
  "data": [
    "https://s3.us-east-2.amazonaws.com/bms-app-storage/users/{uuid}/profile/file1.jpg",
    "https://s3.us-east-2.amazonaws.com/bms-app-storage/users/{uuid}/profile/file2.png"
  ],
  "message": "Files listed successfully"
}
```

### 4. Delete File
**DELETE** `/api/v1/files/delete?fileUrl={encoded-url}`

**Response:**
```json
{
  "success": true,
  "data": "File deleted successfully",
  "message": "File deleted successfully"
}
```

### 5. Health Check
**GET** `/api/v1/files/health`

**Response:**
```json
{
  "success": true,
  "data": "File service is running",
  "message": "File service is healthy"
}
```

## Configuration

### Environment Variables
Set these environment variables:

```bash
export AWS_S3_BUCKET_NAME=bms-app-storage
export AWS_S3_REGION=us-east-2
export AWS_S3_ACCESS_KEY=your-access-key
export AWS_S3_SECRET_KEY=your-secret-key
export AWS_S3_BASE_URL=https://s3.us-east-2.amazonaws.com
```

### Application Properties
The application.properties uses environment variables for security:

```properties
aws.s3.bucket-name=${AWS_S3_BUCKET_NAME:bms-app-storage}
aws.s3.region=${AWS_S3_REGION:us-east-2}
aws.s3.access-key=${AWS_S3_ACCESS_KEY:your-access-key}
aws.s3.secret-key=${AWS_S3_SECRET_KEY:your-secret-key}
aws.s3.base-url=${AWS_S3_BASE_URL:https://s3.us-east-2.amazonaws.com}
```

## File Types & Validation

### Supported File Types
- **Images:** JPEG, JPG, PNG, GIF, WebP
- **Documents:** PDF, DOC, DOCX

### File Restrictions
- Maximum file size: 10MB
- File type validation based on MIME type
- Unique filename generation with timestamp + UUID

### Security Features
- JWT authentication required for all operations
- User-scoped file access (users can only access their own files)
- File type validation
- Size restrictions

## Frontend Integration

### File Upload Example (JavaScript)
```javascript
const uploadFile = async (file, fileType, description) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('uploadRequest', JSON.stringify({
    fileType: fileType,
    description: description
  }));

  const response = await fetch('/api/v1/files/upload', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    },
    body: formData
  });

  return response.json();
};
```

### File Download Example (JavaScript)
```javascript
const downloadFile = async (fileUrl) => {
  const encodedUrl = encodeURIComponent(fileUrl);
  const response = await fetch(`/api/v1/files/image?fileUrl=${encodedUrl}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });

  if (response.ok) {
    const blob = await response.blob();
    const imageUrl = URL.createObjectURL(blob);
    return imageUrl;
  }
  throw new Error('Failed to download file');
};
```

## Docker Configuration

### Environment Variables in Docker
```bash
docker run -d \
  -e AWS_S3_BUCKET_NAME=bms-app-storage \
  -e AWS_S3_REGION=us-east-2 \
  -e AWS_S3_ACCESS_KEY=your-access-key \
  -e AWS_S3_SECRET_KEY=your-secret-key \
  -e AWS_S3_BASE_URL=https://s3.us-east-2.amazonaws.com \
  -p 8080:8080 \
  anoshorpaul/bms-core-service:latest
```

## AWS Setup Instructions

### 1. Create S3 Bucket
```bash
aws s3 mb s3://bms-app-storage --region us-east-2
```

### 2. Set Bucket Policy
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowBMSBackendAccess",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::ACCOUNT_ID:user/bms-backend-user"
      },
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::bms-app-storage",
        "arn:aws:s3:::bms-app-storage/*"
      ]
    }
  ]
}
```

### 3. Create IAM User & Policy
Create user `bms-backend-user` with programmatic access and attach this policy:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket",
        "s3:GetObjectVersion",
        "s3:DeleteObjectVersion"
      ],
      "Resource": [
        "arn:aws:s3:::bms-app-storage",
        "arn:aws:s3:::bms-app-storage/*"
      ]
    }
  ]
}
```

## Testing

### Test File Upload
```bash
curl -X POST "http://localhost:8080/api/v1/files/upload" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@test-image.jpg" \
  -F 'uploadRequest={"fileType":"profile","description":"Test upload"}'
```

### Test File Download
```bash
curl -X GET "http://localhost:8080/api/v1/files/image?fileUrl=ENCODED_FILE_URL" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output downloaded_image.jpg
```

## Cost Considerations

### S3 Pricing (us-east-2)
- **Storage:** ~$0.023 per GB/month
- **Requests:** ~$0.0004 per 1,000 PUT requests
- **Data Transfer:** Free within AWS, $0.09/GB outbound

### Estimated Monthly Costs
- **Small app** (100 users, 1GB total): ~$2-5/month
- **Medium app** (1,000 users, 10GB total): ~$20-40/month
- **Large app** (10,000 users, 100GB total): ~$200-400/month

## Troubleshooting

### Common Issues
1. **Access Denied:** Check IAM policies and credentials
2. **File Not Found:** Verify bucket name and region
3. **Upload Fails:** Check file size limits and content type
4. **CORS Issues:** Configure bucket CORS if accessing from browser

### Debug Environment Variables
```bash
echo $AWS_S3_BUCKET_NAME
echo $AWS_S3_REGION
echo $AWS_S3_ACCESS_KEY
```

## Security Best Practices

1. **Never commit credentials** to version control
2. **Use environment variables** for sensitive configuration
3. **Implement proper IAM policies** with minimal required permissions
4. **Enable S3 bucket logging** for audit trails
5. **Use HTTPS** for all file operations
6. **Validate file types** and sizes before upload
7. **Implement rate limiting** for upload endpoints

---

**Note:** Ensure application.properties is properly gitignored to prevent credential exposure.