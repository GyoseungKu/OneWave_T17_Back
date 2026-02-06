package org.syu_likelion.OneWave.analysis.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiAnalysisResponse {
    private Long analysisId;
    private Long ideaId;
    private int marketScore;
    private int innovationScore;
    private int feasibilityScore;
    private int totalScore;
    private String strength1;
    private String strength2;
    private String improvements1;
    private String improvements2;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
