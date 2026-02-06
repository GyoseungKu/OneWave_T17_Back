package org.syu_likelion.OneWave.feed.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.syu_likelion.OneWave.idea.IdeaCategory;

@Getter
@Setter
public class FeedListItemResponse {
    private Long feedId;
    private String title;
    private String problem;
    private IdeaCategory category;
    private String authorName;
    private LocalDateTime createdAt;
    private long commentCount;
    private long likeCount;
    private Integer totalScore;
    private boolean likedByMe;
}
