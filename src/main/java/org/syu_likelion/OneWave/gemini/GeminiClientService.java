package org.syu_likelion.OneWave.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GeminiClientService {
    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public GeminiClientService(
        WebClient.Builder builder,
        @Value("${gemini.base-url}") String baseUrl,
        @Value("${gemini.api.key}") String apiKey,
        @Value("${gemini.model}") String model
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini API key not configured");
        }

        GeminiRequest request = new GeminiRequest(
            List.of(new Content("user", List.of(new Part(prompt))))
        );

        GeminiResponse response = webClient.post()
            .uri("/models/{model}:generateContent", model)
            .header("x-goog-api-key", apiKey)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(GeminiResponse.class)
            .block();

        if (response == null
            || response.candidates == null
            || response.candidates.isEmpty()
            || response.candidates.get(0).content == null
            || response.candidates.get(0).content.parts == null
            || response.candidates.get(0).content.parts.isEmpty()
            || response.candidates.get(0).content.parts.get(0).text == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini response empty");
        }

        return response.candidates.get(0).content.parts.get(0).text;
    }

    public record GeminiRequest(List<Content> contents) {}

    public record Content(String role, List<Part> parts) {}

    public record Part(String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiResponse {
        public List<Candidate> candidates;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        public Content content;
    }
}
