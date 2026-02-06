package org.syu_likelion.OneWave.feed.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyTeamResponse {
    private Long feedId;
    private String ideaTitle;
    private String ownerName;
    private String stack;
    private LocalDateTime joinedAt;
}
