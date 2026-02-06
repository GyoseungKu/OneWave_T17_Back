package org.syu_likelion.OneWave.startup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.syu_likelion.OneWave.startup.dto.StartupRecommendationResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Startup Recommendation", description = "Recommend startup support programs")
@RestController
@RequestMapping("/api/ideas")
@SecurityRequirement(name = "bearerAuth")
public class StartupRecommendationController {
    private final StartupRecommendationService recommendationService;

    public StartupRecommendationController(StartupRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{ideaId}/recommendations")
    @Operation(summary = "Recommend programs", description = "Recommend startup support programs for an idea")
    public StartupRecommendationResponse recommend(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long ideaId,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Integer perPage
    ) {
        return recommendationService.recommend(userDetails.getUsername(), ideaId, limit, perPage);
    }
}
