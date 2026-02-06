package org.syu_likelion.OneWave.idea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.syu_likelion.OneWave.idea.IdeaCategory;
import org.syu_likelion.OneWave.idea.IdeaStage;

@Getter
@Setter
public class IdeaCreateRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String problem;

    @NotBlank
    @Size(max = 1000)
    private String targetCustomer;

    @NotBlank
    @Size(max = 3000)
    private String solution;

    @NotBlank
    @Size(max = 2000)
    private String differentiation;

    @NotNull
    private IdeaCategory category;

    @NotNull
    private IdeaStage stage;
}
