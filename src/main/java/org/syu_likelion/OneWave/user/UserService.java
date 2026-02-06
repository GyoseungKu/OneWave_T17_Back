package org.syu_likelion.OneWave.user;

import org.syu_likelion.OneWave.auth.email.EmailAuthCodeService;
import org.syu_likelion.OneWave.auth.email.EmailAuthPurpose;
import org.syu_likelion.OneWave.user.dto.UpdateUserRequest;
import org.syu_likelion.OneWave.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailAuthCodeService emailAuthCodeService;
    private final ProfileImageStorage profileImageStorage;
    private final org.syu_likelion.OneWave.feed.FeedApplicationRepository feedApplicationRepository;
    private final String profileBaseUrl;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        EmailAuthCodeService emailAuthCodeService,
        ProfileImageStorage profileImageStorage,
        org.syu_likelion.OneWave.feed.FeedApplicationRepository feedApplicationRepository,
        @Value("${r2.base-url}") String profileBaseUrl
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailAuthCodeService = emailAuthCodeService;
        this.profileImageStorage = profileImageStorage;
        this.feedApplicationRepository = feedApplicationRepository;
        this.profileBaseUrl = profileBaseUrl;
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

    public String uploadProfileImage(String email, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ProfileImageStorage.SavedProfileImage saved = profileImageStorage.save(file);
        String url = buildProfileUrl(saved.relativePath());
        user.setProfileImageUrl(url);
        userRepository.save(user);
        return url;
    }

    public void setProfileImageUrl(String email, String imageUrl) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setProfileImageUrl(imageUrl);
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
        response.setProfileImageUrl(user.getProfileImageUrl());
        return response;
    }

    public java.util.List<org.syu_likelion.OneWave.feed.dto.MyTeamResponse> listMyTeams(String email) {
        return feedApplicationRepository.findApprovedByUserEmailWithFeed(email)
            .stream()
            .map(application -> {
                org.syu_likelion.OneWave.feed.dto.MyTeamResponse response =
                    new org.syu_likelion.OneWave.feed.dto.MyTeamResponse();
                response.setFeedId(application.getFeed().getFeedId());
                response.setIdeaTitle(application.getFeed().getIdea().getTitle());
                response.setOwnerName(application.getFeed().getUser().getName());
                response.setStack(application.getStack());
                response.setJoinedAt(application.getUpdatedAt());
                return response;
            })
            .toList();
    }

    private String buildProfileUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        String base = profileBaseUrl;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return base + "/" + relativePath;
    }
}
