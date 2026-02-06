package org.syu_likelion.OneWave.user;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "User", description = "My page and user profile API")
@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Get the current user's profile")
    public UserResponse me(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getMe(userDetails.getUsername());
    }

    @PatchMapping("/me")
    @Operation(summary = "Update my profile", description = "Update the current user's profile")
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
    @Operation(summary = "Request password change email", description = "Send a verification code for password change")
    public MessageResponse requestPasswordChangeEmail(@AuthenticationPrincipal UserDetails userDetails) {
        userService.requestPasswordChangeEmail(userDetails.getUsername());
        return new MessageResponse("Verification code has been sent to your email.");
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Change password", description = "Change password after verifying the email code")
    public MessageResponse changePassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(userDetails.getUsername(), request.getCode(), request.getNewPassword());
        return new MessageResponse("Password has been changed.");
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete account", description = "Delete the current user's account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
    }
}
