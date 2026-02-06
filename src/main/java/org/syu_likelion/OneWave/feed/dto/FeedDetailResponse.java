package org.syu_likelion.OneWave.feed.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.syu_likelion.OneWave.idea.IdeaCategory;
import org.syu_likelion.OneWave.idea.IdeaStage;

@Getter
@Setter
public class FeedDetailResponse {
    private Long feedId;
    private String title;
    private String problem;
    private String targetCustomer;
    private String solution;
    private String differentiation;
    private IdeaCategory category;
    private IdeaStage stage;
    private String authorName;
    private long likeCount;
    private boolean likedByMe;

    private java.util.List<FeedMemberResponse> members;
    private java.util.List<FeedPositionStatusResponse> positions;

    private Integer marketScore;
    private Integer innovationScore;
    private Integer feasibilityScore;
    private Integer totalScore;
    private String strength1;
    private String strength2;
    private String improvements1;
    private String improvements2;

    private LocalDateTime createdAt;
}
