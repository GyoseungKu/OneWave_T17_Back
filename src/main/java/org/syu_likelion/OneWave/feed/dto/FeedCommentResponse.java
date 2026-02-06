package org.syu_likelion.OneWave.feed.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedCommentResponse {
    private Long commentId;
    private String authorName;
    private String authorProfileImageUrl;
    private LocalDateTime createdAt;
    private String content;
}
