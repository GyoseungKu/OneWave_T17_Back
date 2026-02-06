package org.syu_likelion.OneWave.auth;

import jakarta.validation.Valid;
import org.syu_likelion.OneWave.auth.dto.AuthResponse;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/signup/email")
    public MessageResponse requestSignupEmail(@Valid @RequestBody EmailRequest request) {
        authService.requestSignupEmail(request);
        return new MessageResponse("Verification code has been sent to your email");
    }

    @PostMapping("/signup/email/verify")
    public MessageResponse verifySignupEmail(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifySignupEmail(request);
        return new MessageResponse("Email verification completed");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/password/forgot")
    public MessageResponse requestPasswordReset(@Valid @RequestBody EmailRequest request) {
        authService.requestPasswordResetEmail(request);
        return new MessageResponse("Verification code has been sent to your email");
    }

    @PostMapping("/password/reset")
    public MessageResponse resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("Password has been reset");
    }
}
