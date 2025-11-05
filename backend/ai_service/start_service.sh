#!/bin/bash
# Simple startup script - Linus style

echo "Starting Ollama AI Service..."

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 not found"
    exit 1
fi

# Check Ollama
if ! curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "Warning: Ollama not running, trying to start..."
    if command -v ollama &> /dev/null; then
        ollama serve &
        sleep 3
    else
        echo "Error: Ollama not installed"
        exit 1
    fi
fi

# Install dependencies
echo "Installing dependencies..."
pip3 install -r requirements.txt --quiet

# Check model
echo "Checking Ollama model..."
if ! ollama list | grep -q "llama3.2"; then
    echo "Downloading llama3.2..."
    ollama pull llama3.2:latest
fi

# Start service
echo "Starting AI service on port 8000..."
python3 ollama_service.py
