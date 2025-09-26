# CloudFront Setup Guide for BMS Backend

## Benefits of CloudFront for Image Loading
- ✅ **Fast Loading**: Images cached at edge locations globally
- ✅ **Secure**: S3 bucket stays private with Origin Access Control
- ✅ **Cost Effective**: Reduces S3 data transfer costs
- ✅ **No URL Expiration**: Unlike pre-signed URLs
- ✅ **Auto Compression**: Gzip/Brotli compression available

## AWS Console Setup Steps

### Step 1: Create CloudFront Distribution
1. Go to **CloudFront** service in AWS Console
2. Click **Create Distribution**
3. Configure:
   - **Origin Domain**: Select your S3 bucket `bms-app-storage`
   - **Origin Access**: Choose "Origin access control settings"
   - **Origin Access Control**: Create new OAC named `bms-s3-oac`
   - **Viewer Protocol Policy**: "Redirect HTTP to HTTPS"
   - **Cache Policy**: "Managed-CachingOptimized"
   - **Origin Request Policy**: "Managed-CORS-S3Origin"

### Step 2: Update S3 Bucket Policy
After creating the distribution, AWS will show you a bucket policy to add. It will look like:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowCloudFrontServicePrincipal",
            "Effect": "Allow",
            "Principal": {
                "Service": "cloudfront.amazonaws.com"
            },
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::bms-app-storage/*",
            "Condition": {
                "StringEquals": {
                    "AWS:SourceArn": "arn:aws:cloudfront::ACCOUNT-ID:distribution/DISTRIBUTION-ID"
                }
            }
        }
    ]
}
```

### Step 3: Update Environment Variables
After CloudFront deployment (5-15 minutes), update your environment:

```bash
# Your CloudFront domain will look like: d1234567890abc.cloudfront.net
export AWS_CLOUDFRONT_DOMAIN=d1234567890abc.cloudfront.net
export AWS_CLOUDFRONT_ENABLED=true
```

## How It Works

### Before (Direct S3):
```
User Request → S3 Bucket (us-east-2) → Image
```

### After (CloudFront):
```
User Request → CloudFront Edge (Global) → Cached Image
             ↓ (Cache Miss)
             S3 Bucket → Cache at Edge
```

## URL Format Changes

### Before:
```
https://s3.us-east-2.amazonaws.com/bms-app-storage/users/uuid/profile/image.jpg
```

### After:
```
https://d1234567890abc.cloudfront.net/users/uuid/profile/image.jpg
```

## Performance Impact
- **First Load**: Similar to S3 (cache miss)
- **Subsequent Loads**: 50-90% faster (cached at edge)
- **Global Users**: Dramatic improvement for non-US users

## Testing
1. Deploy CloudFront and update environment variables
2. Upload a new image
3. Check that URLs return CloudFront domain
4. Verify images load quickly on subsequent requests

## Cost Considerations
- CloudFront: ~$0.085/GB (first 10TB)
- S3 Transfer: ~$0.09/GB
- **Net Effect**: Slight savings + much better performance

## Fallback
If CloudFront is disabled (`AWS_CLOUDFRONT_ENABLED=false`), the system automatically falls back to direct S3 URLs.