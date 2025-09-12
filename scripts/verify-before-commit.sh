#!/bin/bash

# Pre-commit verification script for BMS Backend
# Run this before committing to ensure no secrets are exposed

echo "🔍 Checking for potential security issues..."

# Check for hardcoded secrets (excluding empty passwords and templates)
echo "Checking for hardcoded secrets..."
if grep -r "password.*=" --include="*.properties" --include="*.yml" services/ | \
   grep -v "password=$" | \
   grep -v "password=\${" | \
   grep -v "your-app-password" | \
   grep -v "YOUR_APP_PASSWORD" | \
   grep -v "password=guest" | \
   grep -v "target/"; then
    echo "❌ WARNING: Potential hardcoded passwords found!"
    exit 1
fi

# Check for JWT secrets
echo "Checking for hardcoded JWT secrets..."
if grep -r "jwt.secret=" --include="*.properties" services/ | \
   grep -v "CHANGE_THIS" | \
   grep -v "JWT_SECRET" | \
   grep -v "target/"; then
    echo "❌ WARNING: Hardcoded JWT secret found!"
    exit 1
fi

# Check for database files
echo "Checking for database files..."
if find . -name "*.db" -o -name "*.mv.db" -o -name "*.trace.db" | head -1 | grep -q .; then
    echo "❌ WARNING: Database files found! These should be in .gitignore"
    find . -name "*.db" -o -name "*.mv.db" -o -name "*.trace.db"
    exit 1
fi

# Check for environment-specific configs
echo "Checking for environment-specific configs..."
if find . -name "application-local.properties" -o -name "application-prod.properties" | head -1 | grep -q .; then
    echo "❌ WARNING: Environment-specific config files found!"
    find . -name "application-local.properties" -o -name "application-prod.properties"
    exit 1
fi

# Check for .env files
echo "Checking for .env files..."
if find . -name ".env*" | head -1 | grep -q .; then
    echo "❌ WARNING: .env files found!"
    find . -name ".env*"
    exit 1
fi

echo "✅ All security checks passed!"
echo "Safe to commit to GitHub! 🚀"