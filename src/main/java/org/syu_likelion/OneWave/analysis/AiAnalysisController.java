package org.syu_likelion.OneWave.analysis;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.syu_likelion.OneWave.analysis.dto.AiAnalysisResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI Analysis", description = "AI analysis for ideas")
@RestController
@RequestMapping("/api/ideas")
@SecurityRequirement(name = "bearerAuth")
public class AiAnalysisController {
    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/{ideaId}/analysis")
    @Operation(summary = "Analyze idea", description = "Analyze an idea and store the result")
    public AiAnalysisResponse analyze(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long ideaId
    ) {
        return aiAnalysisService.analyzeIdea(userDetails.getUsername(), ideaId);
    }

    @GetMapping("/{ideaId}/analysis")
    @Operation(summary = "Get analysis result", description = "Get the latest analysis result for an idea")
    public AiAnalysisResponse getAnalysis(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long ideaId
    ) {
        return aiAnalysisService.getAnalysis(userDetails.getUsername(), ideaId);
    }
}
