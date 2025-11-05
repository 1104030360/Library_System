#!/bin/bash
# Ollama Model Verification Script
# Check if container properly mounted local model directory

set -e

echo "========================================"
echo "  Ollama Model Verification"
echo "========================================"
echo ""

echo "🔍 Checking Ollama container status..."
docker-compose ps ollama-base

echo ""
echo "📂 Checking mounted model directory..."
docker-compose exec ollama-base ls -lh /root/.ollama/models || echo "⚠️  Models directory not accessible"

echo ""
echo "📋 Listing available models:"
docker-compose exec ollama-base ollama list

echo ""
echo "✅ Verification complete!"
echo ""
echo "💡 Tips:"
echo "  - If you see 11 models, mounting is successful"
echo "  - Total model size should be around 33GB"
echo "  - Primary model in use: llama3.2:latest"
