package org.syu_likelion.OneWave.auth;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.syu_likelion.OneWave.auth.dto.AuthResponse;
import org.syu_likelion.OneWave.auth.dto.EmailCheckResponse;
import org.syu_likelion.OneWave.auth.dto.EmailRequest;
import org.syu_likelion.OneWave.auth.dto.EmailVerifyRequest;
import org.syu_likelion.OneWave.auth.dto.LoginRequest;
import org.syu_likelion.OneWave.auth.dto.MessageResponse;
import org.syu_likelion.OneWave.auth.dto.PasswordResetRequest;
import org.syu_likelion.OneWave.auth.dto.SignupRequest;
import org.syu_likelion.OneWave.auth.dto.SignupResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Authentication and email verification API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up", description = "Complete signup after email verification")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/signup/email/check")
    @Operation(summary = "Check email availability", description = "Check if the email is available for signup")
    public EmailCheckResponse checkSignupEmail(@Valid @RequestBody EmailRequest request) {
        return authService.checkEmailAvailability(request);
    }

    @PostMapping("/signup/email")
    @Operation(summary = "Request signup email code", description = "Send verification code for signup")
    public MessageResponse requestSignupEmail(@Valid @RequestBody EmailRequest request) {
        authService.requestSignupEmail(request);
        return new MessageResponse("Verification code has been sent to your email");
    }

    @PostMapping("/signup/email/verify")
    @Operation(summary = "Verify signup email code", description = "Verify email code before signup")
    public MessageResponse verifySignupEmail(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifySignupEmail(request);
        return new MessageResponse("Email verification completed");
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with email and password")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "Request password reset code", description = "Send verification code for password reset")
    public MessageResponse requestPasswordReset(@Valid @RequestBody EmailRequest request) {
        authService.requestPasswordResetEmail(request);
        return new MessageResponse("Verification code has been sent to your email");
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset password", description = "Reset password with email and verification code")
    public MessageResponse resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("Password has been reset");
    }
}
