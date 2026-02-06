package org.syu_likelion.OneWave.feed.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.syu_likelion.OneWave.feed.FeedApplicationStatus;

@Getter
@Setter
public class FeedApplicationResponse {
    private Long applicationId;
    private String applicantName;
    private String applicantProfileImageUrl;
    private String stack;
    private FeedApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
