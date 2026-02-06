package org.syu_likelion.OneWave.feed;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.syu_likelion.OneWave.auth.dto.MessageResponse;
import org.syu_likelion.OneWave.feed.dto.FeedCommentRequest;
import org.syu_likelion.OneWave.feed.dto.FeedCommentResponse;
import org.syu_likelion.OneWave.feed.dto.FeedCreateRequest;
import org.syu_likelion.OneWave.feed.dto.FeedDetailResponse;
import org.syu_likelion.OneWave.feed.dto.FeedListItemResponse;
import org.syu_likelion.OneWave.feed.dto.FeedApplyRequest;
import org.syu_likelion.OneWave.feed.dto.FeedApplicationResponse;
import org.syu_likelion.OneWave.feed.dto.MyApplicationResponse;
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

@Tag(name = "Feed", description = "Feed for ideas and AI analysis")
@RestController
@RequestMapping("/api/feeds")
@SecurityRequirement(name = "bearerAuth")
public class FeedController {
    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @PostMapping
    @Operation(summary = "Create feed", description = "Register an idea and its AI analysis into the feed")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Create feed with recruiting positions",
        required = true,
        content = @io.swagger.v3.oas.annotations.media.Content(
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = "{\"ideaId\":1,\"positions\":[{\"stack\":\"Frontend\",\"capacity\":1},{\"stack\":\"Backend\",\"capacity\":2},{\"stack\":\"AI\",\"capacity\":1}]}"
            )
        )
    )
    public FeedDetailResponse createFeed(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody FeedCreateRequest request
    ) {
        return feedService.createFeed(userDetails.getUsername(), request);
    }

    @GetMapping
    @Operation(summary = "List feeds", description = "Get feed list with summary info")
    public List<FeedListItemResponse> listFeeds(@AuthenticationPrincipal UserDetails userDetails) {
        return feedService.listFeeds(userDetails.getUsername());
    }

    @GetMapping("/{feedId}")
    @Operation(summary = "Get feed detail", description = "Get full idea and AI analysis details")
    public FeedDetailResponse getFeedDetail(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId
    ) {
        return feedService.getFeedDetail(feedId, userDetails.getUsername());
    }

    @DeleteMapping("/{feedId}")
    @Operation(summary = "Delete feed", description = "Delete a feed owned by the current user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeed(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId
    ) {
        feedService.deleteFeed(userDetails.getUsername(), feedId);
    }

    @PostMapping("/{feedId}/likes")
    @Operation(summary = "Like feed", description = "Like a feed once per user")
    public MessageResponse likeFeed(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId
    ) {
        feedService.likeFeed(userDetails.getUsername(), feedId);
        return new MessageResponse("Liked");
    }

    @DeleteMapping("/{feedId}/likes")
    @Operation(summary = "Unlike feed", description = "Cancel like on a feed")
    public MessageResponse unlikeFeed(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId
    ) {
        feedService.unlikeFeed(userDetails.getUsername(), feedId);
        return new MessageResponse("Unliked");
    }

    @PostMapping("/{feedId}/applications")
    @Operation(summary = "Apply to feed", description = "Apply to a feed with selected stack")
    public MessageResponse applyToFeed(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId,
        @Valid @RequestBody FeedApplyRequest request
    ) {
        feedService.applyToFeed(userDetails.getUsername(), feedId, request);
        return new MessageResponse("Applied");
    }

    @GetMapping("/{feedId}/applications")
    @Operation(summary = "List feed applications", description = "List applications for a feed (owner only)")
    public java.util.List<FeedApplicationResponse> listFeedApplications(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId
    ) {
        return feedService.listFeedApplications(userDetails.getUsername(), feedId);
    }

    @PostMapping("/{feedId}/applications/{applicationId}/approve")
    @Operation(summary = "Approve application", description = "Approve a feed application (owner only)")
    public MessageResponse approveApplication(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId,
        @PathVariable Long applicationId
    ) {
        feedService.approveApplication(userDetails.getUsername(), feedId, applicationId);
        return new MessageResponse("Approved");
    }

    @PostMapping("/{feedId}/applications/{applicationId}/reject")
    @Operation(summary = "Reject application", description = "Reject a feed application (owner only)")
    public MessageResponse rejectApplication(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId,
        @PathVariable Long applicationId
    ) {
        feedService.rejectApplication(userDetails.getUsername(), feedId, applicationId);
        return new MessageResponse("Rejected");
    }

    @GetMapping("/applications/me")
    @Operation(summary = "My applications", description = "Get my application status list")
    public java.util.List<MyApplicationResponse> listMyApplications(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return feedService.listMyApplications(userDetails.getUsername());
    }

    @PostMapping("/{feedId}/comments")
    @Operation(summary = "Add comment", description = "Add a comment to a feed")
    public FeedCommentResponse addComment(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long feedId,
        @Valid @RequestBody FeedCommentRequest request
    ) {
        return feedService.addComment(userDetails.getUsername(), feedId, request);
    }

    @GetMapping("/{feedId}/comments")
    @Operation(summary = "List comments", description = "List all comments for a feed")
    public List<FeedCommentResponse> listComments(@PathVariable Long feedId) {
        return feedService.listComments(feedId);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete comment", description = "Delete a comment owned by the current user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long commentId
    ) {
        feedService.deleteComment(userDetails.getUsername(), commentId);
    }
}
