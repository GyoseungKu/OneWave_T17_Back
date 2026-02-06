package org.syu_likelion.OneWave.idea;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.syu_likelion.OneWave.idea.dto.IdeaCreateRequest;
import org.syu_likelion.OneWave.idea.dto.IdeaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Idea", description = "Idea registration and management API")
@RestController
@RequestMapping("/api/ideas")
@SecurityRequirement(name = "bearerAuth")
public class IdeaController {
    private final IdeaService ideaService;

    public IdeaController(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    @PostMapping
    @Operation(summary = "Create idea", description = "Register a new idea for the current user")
    public IdeaResponse createIdea(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody IdeaCreateRequest request
    ) {
        return ideaService.createIdea(userDetails.getUsername(), request);
    }

    @GetMapping
    @Operation(summary = "List my ideas", description = "Get a list of ideas created by the current user")
    public List<IdeaResponse> listMyIdeas(@AuthenticationPrincipal UserDetails userDetails) {
        return ideaService.listMyIdeas(userDetails.getUsername());
    }

    @GetMapping("/{ideaId}")
    @Operation(summary = "Get my idea", description = "Get a single idea created by the current user")
    public IdeaResponse getMyIdea(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long ideaId
    ) {
        return ideaService.getMyIdea(userDetails.getUsername(), ideaId);
    }

    @DeleteMapping("/{ideaId}")
    @Operation(summary = "Delete idea", description = "Delete an idea owned by the current user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIdea(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long ideaId
    ) {
        ideaService.deleteIdea(userDetails.getUsername(), ideaId);
    }
}
