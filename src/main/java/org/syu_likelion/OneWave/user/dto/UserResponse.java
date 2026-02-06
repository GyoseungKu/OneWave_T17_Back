package org.syu_likelion.OneWave.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.syu_likelion.OneWave.user.Gender;

@Getter
@Setter
public class UserResponse {
    private Long userId;
    private String email;
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Gender gender;

    private String profileImageUrl;
}
