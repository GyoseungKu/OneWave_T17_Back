package org.syu_likelion.OneWave.feed;

import java.util.List;
import org.syu_likelion.OneWave.analysis.AiAnalysis;
import org.syu_likelion.OneWave.analysis.AiAnalysisRepository;
import org.syu_likelion.OneWave.feed.dto.FeedApplyRequest;
import org.syu_likelion.OneWave.feed.dto.FeedApplicationResponse;
import org.syu_likelion.OneWave.feed.dto.FeedCommentRequest;
import org.syu_likelion.OneWave.feed.dto.FeedCommentResponse;
import org.syu_likelion.OneWave.feed.dto.FeedCreateRequest;
import org.syu_likelion.OneWave.feed.dto.FeedDetailResponse;
import org.syu_likelion.OneWave.feed.dto.FeedListItemResponse;
import org.syu_likelion.OneWave.feed.dto.FeedMemberResponse;
import org.syu_likelion.OneWave.feed.dto.FeedPositionRequest;
import org.syu_likelion.OneWave.feed.dto.FeedPositionStatusResponse;
import org.syu_likelion.OneWave.feed.dto.MyApplicationResponse;
import org.syu_likelion.OneWave.feed.dto.MyTeamResponse;
import org.syu_likelion.OneWave.idea.Idea;
import org.syu_likelion.OneWave.idea.IdeaRepository;
import org.syu_likelion.OneWave.user.User;
import org.syu_likelion.OneWave.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FeedService {
    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final FeedRecruitPositionRepository feedRecruitPositionRepository;
    private final FeedApplicationRepository feedApplicationRepository;
    private final IdeaRepository ideaRepository;
    private final UserRepository userRepository;
    private final AiAnalysisRepository aiAnalysisRepository;

    public FeedService(
        FeedRepository feedRepository,
        FeedLikeRepository feedLikeRepository,
        FeedCommentRepository feedCommentRepository,
        FeedRecruitPositionRepository feedRecruitPositionRepository,
        FeedApplicationRepository feedApplicationRepository,
        IdeaRepository ideaRepository,
        UserRepository userRepository,
        AiAnalysisRepository aiAnalysisRepository
    ) {
        this.feedRepository = feedRepository;
        this.feedLikeRepository = feedLikeRepository;
        this.feedCommentRepository = feedCommentRepository;
        this.feedRecruitPositionRepository = feedRecruitPositionRepository;
        this.feedApplicationRepository = feedApplicationRepository;
        this.ideaRepository = ideaRepository;
        this.userRepository = userRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
    }

    public FeedDetailResponse createFeed(String email, FeedCreateRequest request) {
        Idea idea = ideaRepository.findByIdeaIdAndUserEmail(request.getIdeaId(), email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));

        if (!aiAnalysisRepository.existsByIdeaIdeaId(idea.getIdeaId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI analysis not found");
        }

        if (feedRepository.findByIdeaIdeaId(idea.getIdeaId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Feed already exists");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Feed feed = new Feed();
        feed.setIdea(idea);
        feed.setUser(user);
        Feed saved = feedRepository.save(feed);
        savePositions(saved, request.getPositions());
        return toDetailResponse(saved, 0L, email);
    }

    public List<FeedListItemResponse> listFeeds(String email) {
        return feedRepository.findAllWithIdeaAndUser()
            .stream()
            .map(feed -> toListItemResponse(feed, email))
            .toList();
    }

    public FeedDetailResponse getFeedDetail(Long feedId, String email) {
        Feed feed = feedRepository.findDetailByFeedId(feedId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));
        long likeCount = feedLikeRepository.countByFeedFeedId(feedId);
        return toDetailResponse(feed, likeCount, email);
    }

    @Transactional
    public void deleteFeed(String email, Long feedId) {
        Feed feed = feedRepository.findByFeedIdAndUserEmail(feedId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));
        feedApplicationRepository.deleteByFeedFeedId(feedId);
        feedLikeRepository.deleteByFeedFeedId(feedId);
        feedCommentRepository.deleteByFeedFeedId(feedId);
        feedRecruitPositionRepository.deleteByFeedFeedId(feedId);
        feedRepository.delete(feed);
    }

    public void likeFeed(String email, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));
        if (feedLikeRepository.existsByFeedFeedIdAndUserEmail(feedId, email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already liked");
        }
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        FeedLike like = new FeedLike();
        like.setFeed(feed);
        like.setUser(user);
        feedLikeRepository.save(like);
    }

    public void unlikeFeed(String email, Long feedId) {
        FeedLike like = feedLikeRepository.findByFeedFeedIdAndUserEmail(feedId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Like not found"));
        feedLikeRepository.delete(like);
    }

    public FeedCommentResponse addComment(String email, Long feedId, FeedCommentRequest request) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        FeedComment comment = new FeedComment();
        comment.setFeed(feed);
        comment.setUser(user);
        comment.setContent(request.getContent());
        FeedComment saved = feedCommentRepository.save(comment);
        return toCommentResponse(saved);
    }

    public List<FeedCommentResponse> listComments(Long feedId) {
        return feedCommentRepository.findAllByFeedFeedIdOrderByCreatedAtAsc(feedId)
            .stream()
            .map(this::toCommentResponse)
            .toList();
    }

    public void deleteComment(String email, Long commentId) {
        FeedComment comment = feedCommentRepository.findByFeedCommentIdAndUserEmail(commentId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        feedCommentRepository.delete(comment);
    }

    public void applyToFeed(String email, Long feedId, FeedApplyRequest request) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));

        if (feedApplicationRepository.findByFeedFeedIdAndUserEmail(feedId, email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already applied");
        }

        FeedRecruitPosition position = feedRecruitPositionRepository.findAllByFeedFeedId(feedId)
            .stream()
            .filter(p -> p.sameStack(request.getStack()))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stack"));

        long approvedCount = feedApplicationRepository.countByFeedFeedIdAndStatusAndStack(
            feedId,
            FeedApplicationStatus.APPROVED,
            position.getStack()
        );
        if (approvedCount >= position.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Position full");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        FeedApplication application = new FeedApplication();
        application.setFeed(feed);
        application.setUser(user);
        application.setStack(position.getStack());
        application.setStatus(FeedApplicationStatus.PENDING);
        feedApplicationRepository.save(application);
    }

    public List<MyApplicationResponse> listMyApplications(String email) {
        return feedApplicationRepository.findAllByUserEmailWithFeed(email)
            .stream()
            .map(this::toMyApplicationResponse)
            .toList();
    }

    public List<FeedApplicationResponse> listFeedApplications(String email, Long feedId) {
        Feed feed = feedRepository.findByFeedIdAndUserEmail(feedId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));
        return feedApplicationRepository.findAllByFeedIdWithUser(feed.getFeedId())
            .stream()
            .map(this::toFeedApplicationResponse)
            .toList();
    }

    public void approveApplication(String email, Long feedId, Long applicationId) {
        Feed feed = feedRepository.findByFeedIdAndUserEmail(feedId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));
        FeedApplication application = feedApplicationRepository
            .findByApplicationIdAndFeedFeedId(applicationId, feed.getFeedId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (application.getStatus() == FeedApplicationStatus.APPROVED) {
            return;
        }

        FeedRecruitPosition position = feedRecruitPositionRepository.findAllByFeedFeedId(feedId)
            .stream()
            .filter(p -> p.sameStack(application.getStack()))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stack"));

        long approvedCount = feedApplicationRepository.countByFeedFeedIdAndStatusAndStack(
            feedId,
            FeedApplicationStatus.APPROVED,
            position.getStack()
        );
        if (approvedCount >= position.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Position full");
        }

        application.setStatus(FeedApplicationStatus.APPROVED);
        feedApplicationRepository.save(application);
    }

    public void rejectApplication(String email, Long feedId, Long applicationId) {
        Feed feed = feedRepository.findByFeedIdAndUserEmail(feedId, email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found"));
        FeedApplication application = feedApplicationRepository
            .findByApplicationIdAndFeedFeedId(applicationId, feed.getFeedId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        application.setStatus(FeedApplicationStatus.REJECTED);
        feedApplicationRepository.save(application);
    }

    public List<MyTeamResponse> listMyTeams(String email) {
        return feedApplicationRepository.findApprovedByUserEmailWithFeed(email)
            .stream()
            .map(this::toMyTeamResponse)
            .toList();
    }

    private FeedListItemResponse toListItemResponse(Feed feed, String email) {
        FeedListItemResponse response = new FeedListItemResponse();
        response.setFeedId(feed.getFeedId());
        response.setTitle(feed.getIdea().getTitle());
        response.setProblem(feed.getIdea().getProblem());
        response.setCategory(feed.getIdea().getCategory());
        response.setAuthorName(feed.getUser().getName());
        response.setCreatedAt(feed.getCreatedAt());
        response.setCommentCount(feedCommentRepository.countByFeedFeedId(feed.getFeedId()));
        response.setLikeCount(feedLikeRepository.countByFeedFeedId(feed.getFeedId()));
        response.setLikedByMe(feedLikeRepository.existsByFeedFeedIdAndUserEmail(feed.getFeedId(), email));
        AiAnalysis analysis = aiAnalysisRepository.findByIdeaIdeaId(feed.getIdea().getIdeaId()).orElse(null);
        response.setTotalScore(analysis == null ? null : analysis.getTotalScore());
        return response;
    }

    private FeedDetailResponse toDetailResponse(Feed feed, long likeCount, String email) {
        FeedDetailResponse response = new FeedDetailResponse();
        response.setFeedId(feed.getFeedId());
        response.setTitle(feed.getIdea().getTitle());
        response.setProblem(feed.getIdea().getProblem());
        response.setTargetCustomer(feed.getIdea().getTargetCustomer());
        response.setSolution(feed.getIdea().getSolution());
        response.setDifferentiation(feed.getIdea().getDifferentiation());
        response.setCategory(feed.getIdea().getCategory());
        response.setStage(feed.getIdea().getStage());
        response.setAuthorName(feed.getUser().getName());
        response.setLikeCount(likeCount);
        response.setLikedByMe(feedLikeRepository.existsByFeedFeedIdAndUserEmail(feed.getFeedId(), email));
        response.setCreatedAt(feed.getCreatedAt());

        AiAnalysis analysis = aiAnalysisRepository.findByIdeaIdeaId(feed.getIdea().getIdeaId()).orElse(null);
        if (analysis != null) {
            response.setMarketScore(analysis.getMarketScore());
            response.setInnovationScore(analysis.getInnovationScore());
            response.setFeasibilityScore(analysis.getFeasibilityScore());
            response.setTotalScore(analysis.getTotalScore());
            response.setStrength1(analysis.getStrength1());
            response.setStrength2(analysis.getStrength2());
            response.setImprovements1(analysis.getImprovements1());
            response.setImprovements2(analysis.getImprovements2());
        }

        response.setMembers(buildMembers(feed.getFeedId()));
        response.setPositions(buildPositionStatus(feed.getFeedId()));
        return response;
    }

    private FeedCommentResponse toCommentResponse(FeedComment comment) {
        FeedCommentResponse response = new FeedCommentResponse();
        response.setCommentId(comment.getFeedCommentId());
        response.setAuthorName(comment.getUser().getName());
        response.setAuthorProfileImageUrl(comment.getUser().getProfileImageUrl());
        response.setCreatedAt(comment.getCreatedAt());
        response.setContent(comment.getContent());
        return response;
    }

    private void savePositions(Feed feed, java.util.List<FeedPositionRequest> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Positions required");
        }
        for (FeedPositionRequest position : positions) {
            FeedRecruitPosition entity = new FeedRecruitPosition();
            entity.setFeed(feed);
            entity.setStack(position.getStack());
            entity.setCapacity(position.getCapacity());
            feedRecruitPositionRepository.save(entity);
        }
    }

    private java.util.List<FeedMemberResponse> buildMembers(Long feedId) {
        return feedApplicationRepository.findApprovedByFeedIdWithUser(feedId)
            .stream()
            .map(app -> {
                FeedMemberResponse member = new FeedMemberResponse();
                member.setName(app.getUser().getName());
                member.setProfileImageUrl(app.getUser().getProfileImageUrl());
                member.setStack(app.getStack());
                return member;
            })
            .toList();
    }

    private java.util.List<FeedPositionStatusResponse> buildPositionStatus(Long feedId) {
        java.util.List<FeedRecruitPosition> positions = feedRecruitPositionRepository.findAllByFeedFeedId(feedId);
        java.util.List<FeedApplication> approved = feedApplicationRepository.findApprovedByFeedIdWithUser(feedId);
        java.util.Map<String, Long> filledMap = approved.stream()
            .collect(java.util.stream.Collectors.groupingBy(FeedApplication::getStack, java.util.stream.Collectors.counting()));

        return positions.stream().map(p -> {
            int filled = filledMap.getOrDefault(p.getStack(), 0L).intValue();
            int remaining = Math.max(0, p.getCapacity() - filled);
            FeedPositionStatusResponse status = new FeedPositionStatusResponse();
            status.setStack(p.getStack());
            status.setCapacity(p.getCapacity());
            status.setFilled(filled);
            status.setRemaining(remaining);
            return status;
        }).toList();
    }

    private FeedApplicationResponse toFeedApplicationResponse(FeedApplication application) {
        FeedApplicationResponse response = new FeedApplicationResponse();
        response.setApplicationId(application.getApplicationId());
        response.setApplicantName(application.getUser().getName());
        response.setApplicantProfileImageUrl(application.getUser().getProfileImageUrl());
        response.setStack(application.getStack());
        response.setStatus(application.getStatus());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }

    private MyApplicationResponse toMyApplicationResponse(FeedApplication application) {
        MyApplicationResponse response = new MyApplicationResponse();
        response.setApplicationId(application.getApplicationId());
        response.setFeedId(application.getFeed().getFeedId());
        response.setIdeaTitle(application.getFeed().getIdea().getTitle());
        response.setStack(application.getStack());
        response.setStatus(application.getStatus());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }

    private MyTeamResponse toMyTeamResponse(FeedApplication application) {
        MyTeamResponse response = new MyTeamResponse();
        response.setFeedId(application.getFeed().getFeedId());
        response.setIdeaTitle(application.getFeed().getIdea().getTitle());
        response.setOwnerName(application.getFeed().getUser().getName());
        response.setStack(application.getStack());
        response.setJoinedAt(application.getUpdatedAt());
        return response;
    }
}
