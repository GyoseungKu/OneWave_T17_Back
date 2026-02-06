package org.syu_likelion.OneWave.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedApplyRequest {
    @NotBlank
    @Size(max = 50)
    private String stack;
}
