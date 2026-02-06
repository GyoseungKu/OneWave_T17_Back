package org.syu_likelion.OneWave.feed.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedCreateRequest {
    @NotNull
    private Long ideaId;

    @Valid
    @NotEmpty
    private List<FeedPositionRequest> positions;
}
