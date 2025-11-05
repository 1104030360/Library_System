# AI Service Dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Copy requirements
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy service code
COPY ollama_service_streaming.py .
COPY rag_prompt_builder.py .

# Expose port
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8888/health || exit 1

# Environment variable configuration
ENV OLLAMA_URL=http://ollama-base:11434
ENV MODEL=llama3.2:latest
ENV MAX_RETRIES=2
ENV RETRY_DELAY=1
ENV FLASK_ENV=production

# Start service (streaming mode for real-time generation viewing)
CMD ["python", "ollama_service_streaming.py"]
