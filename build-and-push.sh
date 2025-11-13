#!/bin/bash

# Multi-platform build and push script for BMS services
set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}🐋 Building and pushing BMS Docker images (multi-platform)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if buildx is available
if ! docker buildx version &> /dev/null; then
    echo -e "${RED}Error: docker buildx is not available${NC}"
    exit 1
fi

# Create buildx builder if it doesn't exist
if ! docker buildx inspect bms-builder &> /dev/null; then
    echo -e "${BLUE}Creating buildx builder...${NC}"
    docker buildx create --name bms-builder --use
fi

# Use the builder
docker buildx use bms-builder

# Build and push core-service
echo -e "${BLUE}📦 Building core-service...${NC}"
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t anoshorpaul/bms-core-service:latest \
    --push \
    ./services/core-service

echo -e "${GREEN}✅ Core service built and pushed${NC}"

# Build and push payment-service
echo -e "${BLUE}📦 Building payment-service...${NC}"
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t anoshorpaul/bms-payment-service:latest \
    --push \
    ./services/payment-service

echo -e "${GREEN}✅ Payment service built and pushed${NC}"

echo ""
echo -e "${GREEN}🎉 All services built and pushed successfully!${NC}"
echo ""
echo "Images pushed:"
echo "  - anoshorpaul/bms-core-service:latest (linux/amd64, linux/arm64)"
echo "  - anoshorpaul/bms-payment-service:latest (linux/amd64, linux/arm64)"
echo ""
echo "To deploy, run:"
echo "  docker-compose -f docker-compose.prod.yml up -d"
