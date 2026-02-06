package org.syu_likelion.OneWave.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String newPassword;
}
