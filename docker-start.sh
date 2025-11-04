#!/bin/bash
# Docker Compose One-Click Startup Script

set -e

echo "========================================"
echo "  Library System Docker Startup"
echo "========================================"
echo ""

# 1. Build and start all services
echo "📦 Building and starting services..."
docker-compose up --build -d

echo ""
echo "⏳ Waiting for services to start (60 seconds)..."
sleep 60

# 2. Check service status
echo ""
echo "🔍 Checking service status..."
docker-compose ps

# 3. Verify Ollama model mounting
echo ""
echo "🤖 Verifying Ollama models..."
./verify-ollama.sh 2>/dev/null || echo "⚠️  Ollama verification skipped (run manually if needed)"

echo ""
echo "========================================"
echo "  ✅ All Services Started"
echo "========================================"
echo ""
echo "Access services:"
echo "  - Frontend: http://localhost"
echo "  - Backend API: http://localhost:7070"
echo "  - AI Service: http://localhost:8888"
echo "  - Ollama: http://localhost:11434"
echo ""
echo "View logs:"
echo "  docker-compose logs -f [service-name]"
echo ""
echo "Stop services:"
echo "  docker-compose down"
echo ""
