# Backend Dockerfile for Library Management System
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Install curl for health check
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Copy dependency libraries
COPY lib/ /app/lib/

# Copy source code
COPY backend/src/ /app/src/

# Create bin directory
RUN mkdir -p /app/bin

# Compile Java source code (layered compilation by dependencies)

# Layer 1: Data Model Layer (no dependencies)
RUN javac -cp "lib/*" -d bin \
    src/BookInfo.java \
    src/User.java \
    src/BorrowHistory.java \
    src/BookRating.java \
    src/BookReview.java \
    src/Recommendation.java \
    src/RecommendationTask.java \
    src/Notification.java \
    src/NotificationType.java

# Layer 2: Repository Layer (depends on Layer 1)
RUN javac -cp "lib/*:bin" -d bin \
    src/BookDatabaseRepository.java \
    src/BookFileRepository.java \
    src/UserDatabaseRepository.java \
    src/BorrowHistoryRepository.java \
    src/BookRatingRepository.java \
    src/BookReviewRepository.java \
    src/NotificationRepository.java \
    src/TaskManager.java

# Layer 3: Service Layer (depends on Layer 1, 2)
RUN javac -cp "lib/*:bin" -d bin \
    src/ApiSessionManager.java \
    src/ApiAuthenticationHelper.java \
    src/RecommendationService.java \
    src/RecommendationWebSocketServer.java \
    src/NotificationService.java \
    src/NotificationScheduler.java \
    src/StaticFileHandler.java

# Layer 4: Server Layer (depends on all)
RUN javac -cp "lib/*:bin" -d bin \
    src/LibraryApiServer.java \
    src/SimpleApiServer.java \
    src/HelloApiServer.java

# Create data directory
RUN mkdir -p /app/data

# Expose ports (7070 for HTTP, 7071 for WebSocket)
EXPOSE 7070 7071

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:7070/api/books || exit 1

# Environment variables (can be overridden by docker-compose)
ENV AI_SERVICE_URL=http://ai-service:8888
ENV TZ=Asia/Taipei

# Start application
CMD ["java", "-cp", "lib/*:bin", "LibraryApiServer"]
