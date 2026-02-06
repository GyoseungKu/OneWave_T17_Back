package org.syu_likelion.OneWave.idea.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.syu_likelion.OneWave.idea.IdeaCategory;
import org.syu_likelion.OneWave.idea.IdeaStage;

@Getter
@Setter
public class IdeaResponse {
    private Long ideaId;
    private String title;
    private String problem;
    private String targetCustomer;
    private String solution;
    private String differentiation;
    private IdeaCategory category;
    private IdeaStage stage;
    private LocalDateTime createdAt;
}
