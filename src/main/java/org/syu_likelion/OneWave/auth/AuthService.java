package org.syu_likelion.OneWave.auth;

import org.syu_likelion.OneWave.auth.dto.AuthResponse;
import org.syu_likelion.OneWave.auth.dto.EmailRequest;
import org.syu_likelion.OneWave.auth.dto.EmailVerifyRequest;
import org.syu_likelion.OneWave.auth.dto.LoginRequest;
import org.syu_likelion.OneWave.auth.dto.PasswordResetRequest;
import org.syu_likelion.OneWave.auth.dto.SignupRequest;
import org.syu_likelion.OneWave.auth.dto.SignupResponse;
import org.syu_likelion.OneWave.auth.email.EmailAuthCodeService;
import org.syu_likelion.OneWave.auth.email.EmailAuthPurpose;
import org.syu_likelion.OneWave.config.JwtTokenProvider;
import org.syu_likelion.OneWave.user.User;
import org.syu_likelion.OneWave.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailAuthCodeService emailAuthCodeService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider,
        EmailAuthCodeService emailAuthCodeService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailAuthCodeService = emailAuthCodeService;
    }

    public void requestSignupEmail(EmailRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        emailAuthCodeService.sendCode(request.getEmail(), EmailAuthPurpose.SIGNUP);
    }

    public void verifySignupEmail(EmailVerifyRequest request) {
        emailAuthCodeService.verifyCode(request.getEmail(), request.getCode(), EmailAuthPurpose.SIGNUP);
    }

    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        emailAuthCodeService.consumeVerified(request.getEmail(), EmailAuthPurpose.SIGNUP);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return new SignupResponse("User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer");
    }

    public void requestPasswordResetEmail(EmailRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        emailAuthCodeService.sendCode(request.getEmail(), EmailAuthPurpose.PASSWORD_RESET);
    }

    public void resetPassword(PasswordResetRequest request) {
        emailAuthCodeService.verifyAndConsume(
            request.getEmail(),
            request.getCode(),
            EmailAuthPurpose.PASSWORD_RESET
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
