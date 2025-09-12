# 🗄️ Database Access Guide

After running `docker-compose up -d`, you can access the database in two ways:

## Method 1: H2 Web Console (Built-in)

### Access:
- **URL:** http://localhost:8082/api/v1/h2-console
- **Username:** `sa`
- **Password:** (leave empty)
- **JDBC URL:** `jdbc:h2:file:/app/data/bmsdb`

### Steps:
1. Start the service: `docker-compose up -d`
2. Open browser: http://localhost:8082/api/v1/h2-console
3. Use connection details above
4. Click "Connect"

![H2 Console Connection](https://i.imgur.com/h2console.png)

## Method 2: Adminer (Advanced Database Manager)

### Use the advanced docker-compose file:
```bash
docker-compose -f docker-compose-with-adminer.yml up -d
```

### Access:
- **Adminer URL:** http://localhost:8083
- **System:** H2
- **Server:** Leave empty (for file database)
- **Username:** `sa`  
- **Password:** (leave empty)
- **Database:** `/app/data/bmsdb` (full path to database file)

## 🔍 What You Can See:

### Tables Available:
- `USERS` - All registered users (managers, tenants)
- `PROPERTY_BUILDING` - Property/building information
- `APARTMENT` - Individual units/apartments
- `MAINTENANCE_REQUEST` - Maintenance requests
- `MAINTENANCE_UPDATE` - Updates on maintenance requests
- `SERVICE_CATEGORY` - Categories for maintenance services
- `TENANT_PROPERTY_CONNECTION` - Tenant-property relationships

### Sample Queries:
```sql
-- View all users
SELECT * FROM USERS;

-- View all properties with their managers
SELECT pb.NAME, pb.ADDRESS, u.FIRST_NAME, u.LAST_NAME 
FROM PROPERTY_BUILDING pb 
JOIN USERS u ON pb.MANAGER_ID = u.ID;

-- View maintenance requests with apartment details
SELECT mr.TITLE, mr.STATUS, mr.PRIORITY, a.UNIT_NUMBER, pb.NAME as PROPERTY_NAME
FROM MAINTENANCE_REQUEST mr
JOIN APARTMENT a ON mr.APARTMENT_ID = a.ID
JOIN PROPERTY_BUILDING pb ON a.PROPERTY_ID = pb.ID;

-- View tenant information
SELECT u.FIRST_NAME, u.LAST_NAME, u.EMAIL, a.UNIT_NUMBER, pb.NAME as PROPERTY_NAME
FROM USERS u
JOIN APARTMENT a ON u.EMAIL = a.TENANT_EMAIL
JOIN PROPERTY_BUILDING pb ON a.PROPERTY_ID = pb.ID
WHERE u.ROLE = 'TENANT';
```

## 🛡️ Security Notes:

### For Development/Testing:
- H2 console is enabled for easy database inspection
- Suitable for development and demo purposes

### For Production:
Consider disabling H2 console by removing these environment variables:
```yaml
# Remove these for production:
# - SPRING_H2_CONSOLE_ENABLED=true
# - SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true
```

## 🚀 Quick Setup Commands:

```bash
# Basic setup with H2 console
docker-compose up -d

# Advanced setup with Adminer
docker-compose -f docker-compose-with-adminer.yml up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## 📊 Database Schema Visualization:

```
USERS (id, email, first_name, last_name, role)
├── PROPERTY_BUILDING (id, name, address, manager_id)
│   ├── APARTMENT (id, unit_number, property_id, tenant_email)
│   │   └── MAINTENANCE_REQUEST (id, apartment_id, title, status)
│   │       └── MAINTENANCE_UPDATE (id, request_id, message, update_type)
│   └── PROPERTY_IMAGE (id, property_id, image_url)
├── SERVICE_CATEGORY (id, name, description)
└── TENANT_PROPERTY_CONNECTION (id, tenant_id, property_id)
```

## 💡 Pro Tips:

1. **Data Persistence:** Database files persist in Docker volume `bms-data`
2. **Reset Database:** `docker-compose down -v` removes all data
3. **Backup Database:** Copy files from the volume for backup
4. **Real-time Monitoring:** Use Adminer to watch database changes live

Access either interface after running your Docker container and explore the complete BMS database!