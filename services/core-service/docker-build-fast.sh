#!/bin/bash

# Fast Docker Build Script for BMS Backend
# This script provides different build strategies for different scenarios

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
DOCKERFILE="Dockerfile"
TAG="anoshorpaul/bms-backend"
PUSH=false
CACHE=true
PLATFORM="linux/amd64"
FAST_MODE=false

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --fast          Use fast single-platform build (dev mode)"
    echo "  --multi         Build for multiple platforms (production)"
    echo "  --push          Push to registry after build"
    echo "  --no-cache      Disable build cache"
    echo "  --tag TAG       Custom tag (default: anoshorpaul/bms-backend)"
    echo "  --help          Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --fast                    # Fast local build"
    echo "  $0 --multi --push           # Production build and push"
    echo "  $0 --fast --tag my-app      # Fast build with custom tag"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --fast)
            FAST_MODE=true
            DOCKERFILE="Dockerfile.fast"
            PLATFORM="linux/amd64"
            shift
            ;;
        --multi)
            FAST_MODE=false
            DOCKERFILE="Dockerfile"
            PLATFORM="linux/amd64,linux/arm64"
            shift
            ;;
        --push)
            PUSH=true
            shift
            ;;
        --no-cache)
            CACHE=false
            shift
            ;;
        --tag)
            TAG="$2"
            shift 2
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Build command construction
BUILDX_CMD="docker buildx build"

# Add platform
BUILDX_CMD="$BUILDX_CMD --platform $PLATFORM"

# Add dockerfile
BUILDX_CMD="$BUILDX_CMD -f $DOCKERFILE"

# Add cache options
if [[ "$CACHE" == "true" ]]; then
    if [[ "$FAST_MODE" == "true" ]]; then
        # Local cache for fast builds
        BUILDX_CMD="$BUILDX_CMD --cache-from type=local,src=/tmp/.buildx-cache"
        BUILDX_CMD="$BUILDX_CMD --cache-to type=local,dest=/tmp/.buildx-cache,mode=max"
    else
        # Registry cache for multi-platform builds
        BUILDX_CMD="$BUILDX_CMD --cache-from type=registry,ref=$TAG:cache"
        BUILDX_CMD="$BUILDX_CMD --cache-to type=registry,ref=$TAG:cache,mode=max"
    fi
fi

# Add tags
BUILDX_CMD="$BUILDX_CMD -t $TAG:latest"
BUILDX_CMD="$BUILDX_CMD -t $TAG:aws-s3-integration"

# Add push or load
if [[ "$PUSH" == "true" ]]; then
    BUILDX_CMD="$BUILDX_CMD --push"
else
    if [[ "$FAST_MODE" == "true" ]]; then
        BUILDX_CMD="$BUILDX_CMD --load"
    fi
fi

# Add context
BUILDX_CMD="$BUILDX_CMD ."

# Print build information
print_info "🐋 BMS Backend Docker Build"
print_info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
print_info "Mode: $([ "$FAST_MODE" == "true" ] && echo "FAST (Development)" || echo "MULTI-PLATFORM (Production)")"
print_info "Dockerfile: $DOCKERFILE"
print_info "Platform(s): $PLATFORM"
print_info "Tag: $TAG"
print_info "Cache: $([ "$CACHE" == "true" ] && echo "Enabled" || echo "Disabled")"
print_info "Push: $([ "$PUSH" == "true" ] && echo "Yes" || echo "No")"
print_info "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Create cache directory for fast mode
if [[ "$FAST_MODE" == "true" && "$CACHE" == "true" ]]; then
    mkdir -p /tmp/.buildx-cache
fi

# Execute build
print_info "Starting build..."
echo ""
print_info "Command: $BUILDX_CMD"
echo ""

start_time=$(date +%s)

if eval $BUILDX_CMD; then
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    print_success "Build completed successfully in ${duration}s! 🎉"

    if [[ "$FAST_MODE" == "true" && "$PUSH" == "false" ]]; then
        print_info "Image built locally: $TAG:latest"
        print_info "To run: docker run -p 8080:8080 $TAG:latest"
    elif [[ "$PUSH" == "true" ]]; then
        print_success "Image pushed to registry: $TAG:latest"
    fi
else
    print_error "Build failed! ❌"
    exit 1
fi

# Show optimization tips
echo ""
print_info "💡 Optimization Tips:"
echo "   • Use --fast for development (single platform, local cache)"
echo "   • Use --multi --push for production releases"
echo "   • Keep pom.xml changes minimal to leverage dependency cache"
echo "   • Use .dockerignore to exclude unnecessary files"