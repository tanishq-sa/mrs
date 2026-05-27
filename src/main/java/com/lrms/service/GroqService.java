package com.lrms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.model:llama-3.3-70b-versatile}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public String callGroq(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "ERROR: Groq API Key is not configured. Please set the 'groq.api.key' property in 'application.properties' or define the 'GROQ_API_KEY' environment variable.";
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("temperature", 0.7);

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
                List<?> choices = (List<?>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
                return "Failed to parse choices from Groq completions API response.";
            } else {
                return "Groq API request failed (HTTP " + response.statusCode() + "): " + response.body();
            }
        } catch (Exception e) {
            return "Exception occurred while communicating with Groq API: " + e.getMessage();
        }
    }

    public boolean isKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
