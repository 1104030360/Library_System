#!/bin/bash

# Library Management System - Docker Runner
# Simple script to build and run the application in Docker

cd "/Users/linjunting/Documents/JavaProj 2"

echo "========================================="
echo "  Library API Server - Docker"
echo "========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed!"
    echo "Please install Docker from: https://www.docker.com/get-started"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "❌ Docker is not running!"
    echo "Please start Docker Desktop"
    exit 1
fi

echo "✅ Docker is ready"
echo ""

# Build and start with docker-compose
echo "🔨 Building Docker image..."
docker-compose build

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "🚀 Starting container..."
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "❌ Failed to start container!"
    exit 1
fi

echo ""
echo "✅ Server is starting..."
echo ""
echo "🌐 Access the application at:"
echo "  http://localhost:7070"
echo ""
echo "📊 Container status:"
docker-compose ps
echo ""
echo "📝 View logs:"
echo "  docker-compose logs -f"
echo ""
echo "🛑 Stop server:"
echo "  docker-compose down"
echo ""
