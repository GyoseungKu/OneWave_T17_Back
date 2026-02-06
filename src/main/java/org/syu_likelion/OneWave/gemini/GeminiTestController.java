package org.syu_likelion.OneWave.gemini;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.syu_likelion.OneWave.gemini.dto.GeminiTestRequest;
import org.syu_likelion.OneWave.gemini.dto.GeminiTestResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gemini Test", description = "Simple test endpoint for Gemini API")
@RestController
@RequestMapping("/api/test/gemini")
public class GeminiTestController {
    private final GeminiClientService geminiClientService;

    public GeminiTestController(GeminiClientService geminiClientService) {
        this.geminiClientService = geminiClientService;
    }

    @PostMapping
    @Operation(summary = "Test Gemini", description = "Send a prompt to Gemini and return the first text response")
    public GeminiTestResponse test(@Valid @RequestBody GeminiTestRequest request) {
        String text = geminiClientService.generateText(request.getPrompt());
        return new GeminiTestResponse(text);
    }
}
