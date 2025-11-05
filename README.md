# Modern AI-Powered Library Management System

This project is a library management system that has evolved from a traditional Swing desktop application to a modern web application integrating AI services, real-time recommendations, and contemporary web technologies. The project aims to demonstrate the full lifecycle of a full-stack application, from backend APIs and frontend interactive interfaces to the implementation and containerized deployment of intelligent features.

At a glance:
- **Backend Core API** – Java 17 (native `com.sun.net.httpserver`) + SQLite via JDBC
- **AI Microservice** – Python 3 + Flask + Ollama SDK
- **Frontend** – Vue 3 + TypeScript + Vite + Tailwind CSS
- **Deployment** – Docker / Docker Compose (Nginx, Backend, AI service, Ollama)

---

## What Can Users Do? (User Features)

This system provides comprehensive functionalities for both library patrons and administrators:

-   **Book Browsing and Searching**: Users can browse all books in the collection and quickly find specific books using a real-time search function.
-   **User Authentication**: Provides secure login/logout mechanisms and defines permissions for different user roles (e.g., patron, employee, director).
-   **Borrowing and Returning**: Logged-in users can easily borrow and return books, and the system updates the book status in real-time.
-   **Borrowing History Tracking**: Users can view their complete borrowing history.
-   **Book Ratings and Reviews**: Users can rate books and write reviews to share their reading experiences.
-   **Smart Notification Service**: The system schedules and sends notifications, such as reminders for books that are due soon.
-   **Administrator Functions**: Employees or directors have additional permissions to add new books and manage the collection through a dedicated interface.

---

## Special Features of This Project

This project is more than just a CRUD application; it integrates several modern technologies that make it powerful and unique:

### AI-Powered Q&A Assistant
-   **Domain Knowledge-Enhanced Q&A**: To provide accurate, localized answers, the system's AI assistant uses a knowledge enhancement method. It combines the library's core knowledge base (such as rules from `LibraryRulesRepository` and book data) with a local Large Language Model (LLM) powered by **Ollama**. This design allows the LLM to break free from the limitations of general knowledge and generate highly relevant and reliable answers for scenarios specific to the library.
-   **Python AI Microservice**: This feature is provided by a separate Python (Flask) microservice named `ai_service`, which separates the responsibilities of the backend services.

### Real-time Personalized Book Recommendations
-   **WebSocket Technology**: The system utilizes a `RecommendationWebSocketServer` to push personalized book recommendations to the frontend in real-time via WebSockets.
-   **Asynchronous Tasks**: Recommendation content is generated asynchronously in the background by the `RecommendationService`, ensuring a smooth user experience without impacting main operations.

### Modern Frontend Experience
-   **Vue 3 & Vite & TypeScript**: The frontend is built with the latest Vue 3 framework, combined with Vite for an extremely fast development experience, and uses TypeScript throughout to ensure code robustness and maintainability.
-   **Tailwind CSS**: Uses the utility-first Tailwind CSS framework to create a beautiful, consistent, and highly customizable responsive interface.

### Microservice-Inspired Backend Architecture
-   **Separation of Concerns**: The project adopts a microservice-like design philosophy, separating the core library business logic (Java) from the computationally intensive AI functions (Python). The two communicate via APIs. This design improves the system's flexibility and scalability.

### Complete Containerization Solution
-   **Docker & Docker Compose**: The entire application (including the Vue frontend, Java backend, Python AI service, and Ollama model) is fully containerized. Developers can launch all services with a single `docker-compose up` command in any environment, greatly simplifying deployment and development setup.

---

## System Architecture

This project consists of the following core components:

1.  **Frontend**:
    -   A Single Page Application (SPA) based on **Vue 3**.
    -   Uses **Vite** for project building and development.
    -   Implements a responsive and modern UI with **Tailwind CSS**.
    -   Communicates with the backend API asynchronously.

2.  **Backend - Core API**:
    -   A lightweight RESTful API server built with Java's native `com.sun.net.httpserver`.
    -   Handles core business logic such as book management, user authentication, and borrowing/returning.
    -   Interacts directly with the SQLite database via JDBC.

3.  **Backend - AI Service**:
    -   A separate **Python** server using the **Flask** framework.
    -   Handles the knowledge-enhanced Q&A logic and integrates with **Ollama** to provide LLM inference capabilities.
    -   Specializes in handling natural language queries from the frontend.

4.  **Database**:
    -   Uses **SQLite** as a lightweight, file-based database to store all application data (books, users, reviews, etc.).

5.  **Real-time Layer**:
    -   A **WebSocket** server built into the Java backend for pushing real-time recommendations and notifications to clients.

---

## Quick Start (One-Click Docker Deployment)

Using Docker for deployment is strongly recommended as it is the simplest and most reliable method.

```bash
# Prepare AI service configuration (only needed first time)
cp backend/ai_service/.env_example backend/ai_service/.env

# First, navigate to the project root directory
cd /path/to/your/JavaProj\ 2

# Start all services with a single command (frontend, backend, AI, database)
docker-compose up -d

# Check the running status of all containers
docker-compose ps

# To view the logs of any service
docker-compose logs -f <service_name>  # e.g., docker-compose logs -f backend
```

**Service Access URLs:**

-   **Frontend Application**: `http://localhost:7777` (served by Nginx)
-   **Java Backend API**: `http://localhost:7070`
-   **Python AI Service**: `http://localhost:8888`
-   **Ollama LLM API**: `http://localhost:11434`

**Stopping all services:**

```bash
docker-compose down
```

---

## Testing

The project includes a comprehensive JUnit 5 test suite covering the database, authentication, and API layers.

```bash
# Run all backend tests
./run-tests.sh
```

---

## Technology Stack

-   **Backend**: Java (Native HttpServer), Python (Flask), Gson, JUnit 5
-   **Frontend**: Vue 3, Vite, TypeScript, Pinia, Vue Router, Tailwind CSS, Axios
-   **AI**: Ollama, LangChain (conceptual)
-   **Database**: SQLite
-   **Real-time**: Java WebSocket
-   **Deployment**: Docker, Docker Compose, Nginx

---

> "Talk is cheap. Show me the code." - Linus Torvalds
