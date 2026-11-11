package io.github.seecret1.volgait.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import io.github.seecret1.volgait.config.ConfigProvider;
import org.openqa.selenium.remote.http.HttpMethod;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;

public final class ApiClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI baseUri;

    public ApiClient() {
        this(ConfigProvider.URL_API_SWAGGER);
    }

    ApiClient(String swaggerUrl) {
        URI swaggerUri = URI.create(swaggerUrl);
        this.baseUri = URI.create(swaggerUri.getScheme() + "://" + swaggerUri.getAuthority());
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public ApiResponse createUser(String userName, String password) {
        return send(HttpMethod.POST, "/Account/v1/User", credentials(userName, password), null);
    }

    public ApiResponse generateToken(String userName, String password) {
        return send(HttpMethod.POST, "/Account/v1/GenerateToken", credentials(userName, password), null);
    }

    public ApiResponse authorized(String userName, String password) {
        return send(HttpMethod.POST, "/Account/v1/Authorized", credentials(userName, password), null);
    }

    public ApiResponse getUser(String userId, String token) {
        return send(HttpMethod.GET, "/Account/v1/User/" + encode(userId), null, token);
    }

    public ApiResponse deleteUser(String userId, String token) {
        return send(HttpMethod.DELETE, "/Account/v1/User/" + encode(userId), null, token);
    }

    public ApiResponse getBooks() {
        return send(HttpMethod.GET, "/BookStore/v1/Books", null, null);
    }

    public ApiResponse getBook(String isbn) {
        return send(HttpMethod.GET, "/BookStore/v1/Book?ISBN=" + encode(isbn), null, null);
    }

    public ApiResponse addBooks(String userId, Collection<String> isbns, String token) {
        Collection<Map<String, String>> collection = isbns.stream()
                .map(isbn -> Map.of("isbn", isbn))
                .toList();
        return send(HttpMethod.POST, "/BookStore/v1/Books",
                Map.of("userId", userId, "collectionOfIsbns", collection), token);
    }

    public ApiResponse replaceBook(String currentIsbn, String replacementIsbn, String userId, String token) {
        return send(HttpMethod.PUT, "/BookStore/v1/Books/" + encode(currentIsbn),
                Map.of("userId", userId, "isbn", replacementIsbn), token);
    }

    public ApiResponse deleteBook(String isbn, String userId, String token) {
        return send(HttpMethod.DELETE, "/BookStore/v1/Book",
                Map.of("isbn", isbn, "userId", userId), token);
    }

    public ApiResponse clearBooks(String userId, String token) {
        return send(HttpMethod.DELETE, "/BookStore/v1/Books?UserId=" + encode(userId), null, token);
    }

    private Map<String, String> credentials(String userName, String password) {
        return Map.of("userName", userName, "password", password);
    }

    private ApiResponse send(HttpMethod method, String path, Object body, String token) {
        HttpRequest.Builder request = HttpRequest.newBuilder(baseUri.resolve(path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");
        if (token != null && !token.isBlank()) {
            request.header("Authorization", "Bearer " + token);
        }

        if (body == null) {
            request.method(method.name(), HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method.name(), HttpRequest.BodyPublishers.ofString(toJson(body)));
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String rawBody = response.body() == null ? "" : response.body();
            return new ApiResponse(response.statusCode(), parseBody(rawBody), rawBody);
        } catch (IOException exception) {
            throw new IllegalStateException("DemoQA API request failed: " + method + " " + path, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DemoQA API request interrupted: " + method + " " + path, exception);
        }
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Cannot serialize API request body", exception);
        }
    }

    private JsonNode parseBody(String body) {
        if (body.isBlank()) {
            return MissingNode.getInstance();
        }
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("DemoQA returned invalid JSON: " + body, exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
