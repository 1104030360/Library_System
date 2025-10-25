# Library Management System - Dockerfile
# Multi-stage build: Compile + Runtime
# Following Linus principles: Simple, minimal, efficient

# Stage 1: Build stage
FROM openjdk:17-slim AS builder

# Set working directory
WORKDIR /build

# Copy library dependencies first (better caching)
COPY lib/ ./lib/

# Copy backend source code
COPY backend/src/ ./backend/src/

# Compile Java files
RUN mkdir -p backend/bin && \
    javac -d backend/bin \
    -cp "lib/*:backend/src" \
    backend/src/BookInfo.java && \
    javac -d backend/bin \
    -cp "lib/*:backend/bin" \
    backend/src/ApiSessionManager.java && \
    javac -d backend/bin \
    -cp "lib/*:backend/bin" \
    backend/src/ApiAuthenticationHelper.java && \
    javac -d backend/bin \
    -cp "lib/*:backend/bin" \
    backend/src/BookDatabaseRepository.java && \
    javac -d backend/bin \
    -cp "lib/*:backend/bin" \
    backend/src/StaticFileHandler.java && \
    javac -d backend/bin \
    -cp "lib/*:backend/bin" \
    backend/src/LibraryApiServer.java

# Stage 2: Runtime stage
FROM openjdk:17-slim

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy compiled classes from builder
COPY --from=builder /build/backend/bin/ ./backend/bin/
COPY --from=builder /build/lib/ ./lib/

# Copy web frontend
COPY web/ ./web/

# Create data directory for SQLite database
RUN mkdir -p data

# Expose port 7070
EXPOSE 7070

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:7070/api/status || exit 1

# Run the server
CMD ["java", "-cp", "lib/*:backend/bin", "LibraryApiServer"]
