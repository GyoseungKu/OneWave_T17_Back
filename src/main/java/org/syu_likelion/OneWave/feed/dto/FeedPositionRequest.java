package org.syu_likelion.OneWave.feed.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedPositionRequest {
    @NotBlank
    @Size(max = 50)
    private String stack;

    @Min(1)
    private int capacity;
}
