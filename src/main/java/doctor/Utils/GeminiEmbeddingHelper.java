package doctor.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiEmbeddingHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    @Value("${app.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${app.ai.gemini.embedding-model:gemini-embedding-2}")
    private String embeddingModel;

    @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String baseUrl;

    @Value("${app.ai.gemini.request-timeout-seconds:30}")
    private int requestTimeoutSeconds;

    public double[] embedImage(byte[] imageBytes, String mimeType) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("imageBytes is required");
        }
        String normalizedMimeType = normalizeMimeType(mimeType);

        ensureApiKeyConfigured();

        Map<String, Object> payload =
                Map.of(
                        "model", "models/" + normalizeModel(embeddingModel),
                        "content",
                                Map.of(
                                        "parts",
                                        List.of(
                                                Map.of(
                                                        "inline_data",
                                                        Map.of(
                                                                "mime_type",
                                                                normalizedMimeType,
                                                                "data",
                                                                Base64.getEncoder()
                                                                        .encodeToString(imageBytes))))));

        String responseBody = doEmbedRequest(payload);
        return parseEmbeddingValues(responseBody);
    }

    public double[] embedImageFromUrl(String imageUrl) {
        String normalizedImageUrl = normalizeRequired(imageUrl, "imageUrl");
        HttpRequest request =
                HttpRequest.newBuilder(URI.create(normalizedImageUrl))
                        .GET()
                        .timeout(Duration.ofSeconds(Math.max(5, requestTimeoutSeconds)))
                        .build();
        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Khong the tai anh tu URL", ex);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Khong the tai anh tu URL, status code: " + response.statusCode());
        }

        String contentType = response.headers().firstValue("Content-Type").orElse(null);
        return embedImage(response.body(), normalizeMimeType(contentType));
    }

    private String doEmbedRequest(Map<String, Object> payload) {
        String normalizedBaseUrl = normalizeRequired(baseUrl, "app.ai.gemini.base-url");
        String normalizedModel = normalizeModel(embeddingModel);
        String requestUrl = normalizedBaseUrl + "/" + normalizedModel + ":embedContent";

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(payload);
        } catch (IOException ex) {
            throw new IllegalStateException("Khong the tao payload cho Gemini API", ex);
        }

        HttpRequest request =
                HttpRequest.newBuilder(URI.create(requestUrl))
                        .timeout(Duration.ofSeconds(Math.max(5, requestTimeoutSeconds)))
                        .header("Content-Type", "application/json")
                        .header("x-goog-api-key", apiKey.trim())
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Khong the goi Gemini API", ex);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Gemini embedContent loi, status: "
                            + response.statusCode()
                            + ", body: "
                            + response.body());
        }

        return response.body();
    }

    private double[] parseEmbeddingValues(String responseBody) {
        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (IOException ex) {
            throw new IllegalStateException("Khong doc duoc response tu Gemini API", ex);
        }

        JsonNode valuesNode = root.path("embedding").path("values");
        if (!valuesNode.isArray() || valuesNode.isEmpty()) {
            JsonNode embeddings = root.path("embeddings");
            if (embeddings.isArray() && !embeddings.isEmpty()) {
                valuesNode = embeddings.get(0).path("values");
            }
        }

        if (!valuesNode.isArray() || valuesNode.isEmpty()) {
            throw new IllegalStateException("Gemini khong tra ve embedding values hop le");
        }

        double[] values = new double[valuesNode.size()];
        for (int i = 0; i < valuesNode.size(); i++) {
            values[i] = valuesNode.get(i).asDouble();
        }
        return values;
    }

    private void ensureApiKeyConfigured() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing Gemini API key: app.ai.gemini.api-key");
        }
    }

    private String normalizeModel(String model) {
        String normalizedModel = normalizeRequired(model, "app.ai.gemini.embedding-model");
        if (normalizedModel.startsWith("models/")) {
            return normalizedModel.substring("models/".length());
        }
        return normalizedModel;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return "image/jpeg";
        }
        String normalized = mimeType.trim().toLowerCase();
        int separatorIndex = normalized.indexOf(';');
        if (separatorIndex >= 0) {
            normalized = normalized.substring(0, separatorIndex).trim();
        }
        if (normalized.startsWith("image/")) {
            return normalized;
        }
        return "image/jpeg";
    }
}
