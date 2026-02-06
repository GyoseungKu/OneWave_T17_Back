package org.syu_likelion.OneWave.startup.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartupRecommendationResponse {
    private Long ideaId;
    private Long analysisId;
    private int candidateCount;
    private List<StartupRecommendationItem> items;
}
