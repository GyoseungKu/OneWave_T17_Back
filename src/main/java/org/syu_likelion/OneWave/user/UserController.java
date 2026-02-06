package org.syu_likelion.OneWave.user;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.syu_likelion.OneWave.auth.dto.MessageResponse;
import org.syu_likelion.OneWave.auth.dto.PasswordChangeRequest;
import org.syu_likelion.OneWave.user.dto.UpdateUserRequest;
import org.syu_likelion.OneWave.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getMe(userDetails.getUsername());
    }

    @PatchMapping("/me")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Update current user profile",
        required = true,
        content = @Content(
            schema = @Schema(implementation = UpdateUserRequest.class),
            examples = @ExampleObject(value = "{\"name\":\"string\",\"birthDate\":\"2026-02-06\",\"gender\":\"MALE\"}")
        )
    )
    public UserResponse updateMe(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(userDetails.getUsername(), request);
    }

    @PostMapping("/me/password/email")
    public MessageResponse requestPasswordChangeEmail(@AuthenticationPrincipal UserDetails userDetails) {
        userService.requestPasswordChangeEmail(userDetails.getUsername());
        return new MessageResponse("인증 코드가 이메일로 전송되었습니다.");
    }

    @PatchMapping("/me/password")
    public MessageResponse changePassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(userDetails.getUsername(), request.getCode(), request.getNewPassword());
        return new MessageResponse("비밀번호가 변경되었습니다.");
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
    }
}
