package org.syu_likelion.OneWave.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileImageUpdateRequest {
    @NotBlank
    private String imageUrl;
}
