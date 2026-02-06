package org.syu_likelion.OneWave.gemini.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeminiTestRequest {
    @NotBlank
    private String prompt;
}
