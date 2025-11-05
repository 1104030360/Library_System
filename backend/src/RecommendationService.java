import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI Recommendation Service (Async Version)
 * Non-blocking HTTP client to Python AI service using sendAsync()
 */
public class RecommendationService {

    // Support environment variable for Docker, default to localhost for local development
    private static final String AI_SERVICE_URL =
        System.getenv().getOrDefault("AI_SERVICE_URL", "http://localhost:8888");
    private static final int TIMEOUT_SECONDS = 60;
    private static final int THREAD_POOL_SIZE = 4;

    private final HttpClient client;
    private final Gson gson;
    private final ExecutorService executor;

    public RecommendationService() {
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .executor(executor)  // Use custom executor for async operations
            .build();
        this.gson = new Gson();
    }

    /**
     * Generate personal recommendations for user (ASYNC)
     * Uses HttpClient.sendAsync() for true non-blocking I/O
     */
    public CompletableFuture<List<Recommendation>> getPersonalRecommendationsAsync(
            String userId,
            List<BookInfo> borrowHistory,
            List<BookInfo> availableBooks) {

        try {
            // Build request payload
            JsonObject payload = new JsonObject();

            // User profile
            JsonObject userProfile = new JsonObject();
            JsonArray history = new JsonArray();
            for (BookInfo book : borrowHistory) {
                JsonObject item = new JsonObject();
                item.addProperty("title", book.getTitle());
                history.add(item);
            }
            userProfile.add("borrow_history", history);
            payload.add("user_profile", userProfile);

            // Available books
            JsonArray books = new JsonArray();
            for (BookInfo book : availableBooks) {
                JsonObject b = new JsonObject();
                b.addProperty("id", book.getId());
                b.addProperty("title", book.getTitle());
                b.addProperty("author", book.getAuthor());
                b.addProperty("publisher", book.getPublisher());
                books.add(b);
            }
            payload.add("available_books", books);

            // Build HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AI_SERVICE_URL + "/generate-personal-recommendations"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

            System.out.println("→ Sending async request to AI service for user: " + userId);

            // Send request asynchronously
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // Parse response
                    try {
                        if (response.statusCode() != 200) {
                            System.err.println("AI service error: " + response.statusCode());
                            return new ArrayList<Recommendation>();
                        }

                        JsonObject result = gson.fromJson(response.body(), JsonObject.class);
                        if (!result.get("success").getAsBoolean()) {
                            System.err.println("AI generation failed");
                            return new ArrayList<Recommendation>();
                        }

                        // Extract recommendations
                        List<Recommendation> recommendations = new ArrayList<>();
                        JsonArray recs = result.getAsJsonArray("recommendations");
                        for (JsonElement elem : recs) {
                            JsonObject rec = elem.getAsJsonObject();
                            recommendations.add(new Recommendation(
                                rec.get("book_id").getAsString(),
                                rec.get("reason").getAsString(),
                                rec.get("score").getAsDouble()
                            ));
                        }

                        System.out.println("✓ Generated " + recommendations.size() +
                                         " recommendations for user " + userId + " (async)");
                        return recommendations;

                    } catch (Exception e) {
                        System.err.println("Error parsing AI response: " + e.getMessage());
                        return new ArrayList<Recommendation>();
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Async recommendation error: " + e.getMessage());
                    return new ArrayList<Recommendation>();
                });

        } catch (Exception e) {
            System.err.println("Error building request: " + e.getMessage());
            return CompletableFuture.completedFuture(new ArrayList<Recommendation>());
        }
    }

    /**
     * Generate related book recommendations (ASYNC)
     */
    public CompletableFuture<List<Recommendation>> getRelatedRecommendationsAsync(
            BookInfo currentBook,
            List<BookInfo> relatedBooks) {

        try {
            JsonObject payload = new JsonObject();

            // Current book
            JsonObject current = new JsonObject();
            current.addProperty("id", currentBook.getId());
            current.addProperty("title", currentBook.getTitle());
            current.addProperty("author", currentBook.getAuthor());
            payload.add("current_book", current);

            // Related books
            JsonArray books = new JsonArray();
            for (BookInfo book : relatedBooks) {
                JsonObject b = new JsonObject();
                b.addProperty("id", book.getId());
                b.addProperty("title", book.getTitle());
                b.addProperty("author", book.getAuthor());
                books.add(b);
            }
            payload.add("related_books", books);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AI_SERVICE_URL + "/generate-related-recommendations"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

            System.out.println("→ Sending async request to AI service for book: " + currentBook.getId());

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        if (response.statusCode() != 200) {
                            System.err.println("AI service error: " + response.statusCode());
                            return new ArrayList<Recommendation>();
                        }

                        JsonObject result = gson.fromJson(response.body(), JsonObject.class);
                        if (!result.get("success").getAsBoolean()) {
                            System.err.println("AI generation failed");
                            return new ArrayList<Recommendation>();
                        }

                        List<Recommendation> recommendations = new ArrayList<>();
                        JsonArray recs = result.getAsJsonArray("recommendations");
                        for (JsonElement elem : recs) {
                            JsonObject rec = elem.getAsJsonObject();
                            recommendations.add(new Recommendation(
                                rec.get("book_id").getAsString(),
                                rec.get("reason").getAsString(),
                                rec.get("score").getAsDouble()
                            ));
                        }

                        System.out.println("✓ Generated " + recommendations.size() +
                                         " related recommendations for book " + currentBook.getId() + " (async)");
                        return recommendations;

                    } catch (Exception e) {
                        System.err.println("Error parsing AI response: " + e.getMessage());
                        return new ArrayList<Recommendation>();
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Async related recommendations error: " + e.getMessage());
                    return new ArrayList<Recommendation>();
                });

        } catch (Exception e) {
            System.err.println("Error building request: " + e.getMessage());
            return CompletableFuture.completedFuture(new ArrayList<Recommendation>());
        }
    }

    /**
     * Synchronous wrapper for backward compatibility
     * Blocks until async operation completes
     */
    public List<Recommendation> getPersonalRecommendations(
            String userId,
            List<BookInfo> borrowHistory,
            List<BookInfo> availableBooks) {
        try {
            return getPersonalRecommendationsAsync(userId, borrowHistory, availableBooks).join();
        } catch (Exception e) {
            System.err.println("Sync wrapper error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Synchronous wrapper for backward compatibility
     */
    public List<Recommendation> getRelatedRecommendations(
            BookInfo currentBook,
            List<BookInfo> relatedBooks) {
        try {
            return getRelatedRecommendationsAsync(currentBook, relatedBooks).join();
        } catch (Exception e) {
            System.err.println("Sync wrapper error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check if AI service is healthy
     */
    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AI_SERVICE_URL + "/health"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Shutdown executor when service is stopped
     */
    public void shutdown() {
        executor.shutdown();
        System.out.println("RecommendationService executor shutdown");
    }
}
