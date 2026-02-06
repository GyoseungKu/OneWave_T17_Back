package org.syu_likelion.OneWave.user;

import org.syu_likelion.OneWave.auth.email.EmailAuthCodeService;
import org.syu_likelion.OneWave.auth.email.EmailAuthPurpose;
import org.syu_likelion.OneWave.user.dto.UpdateUserRequest;
import org.syu_likelion.OneWave.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailAuthCodeService emailAuthCodeService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailAuthCodeService emailAuthCodeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailAuthCodeService = emailAuthCodeService;
    }

    public UserResponse updateUser(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(user);
    }

    public void requestPasswordChangeEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        emailAuthCodeService.sendCode(email, EmailAuthPurpose.PASSWORD_CHANGE);
    }

    public void changePassword(String email, String code, String newPassword) {
        emailAuthCodeService.verifyAndConsume(email, code, EmailAuthPurpose.PASSWORD_CHANGE);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setBirthDate(user.getBirthDate());
        response.setGender(user.getGender());
        return response;
    }
}
