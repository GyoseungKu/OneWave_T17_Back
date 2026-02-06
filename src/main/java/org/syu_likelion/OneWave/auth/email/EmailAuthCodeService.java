package org.syu_likelion.OneWave.auth.email;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailAuthCodeService {
    private final EmailAuthCodeRepository repository;
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final long expirationMinutes;

    public EmailAuthCodeService(
        EmailAuthCodeRepository repository,
        JavaMailSender mailSender,
        @Value("${spring.mail.properties.mail.smtp.from:}") String smtpFromAddress,
        @Value("${spring.mail.username:}") String fromAddress,
        @Value("${email.auth.expiration-minutes:10}") long expirationMinutes
    ) {
        this.repository = repository;
        this.mailSender = mailSender;
        if (smtpFromAddress != null && !smtpFromAddress.isBlank()) {
            this.fromAddress = smtpFromAddress;
        } else {
            this.fromAddress = fromAddress;
        }
        this.expirationMinutes = expirationMinutes;
    }

    public void sendCode(String email, EmailAuthPurpose purpose) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        EmailAuthCode authCode = new EmailAuthCode();
        authCode.setEmail(email);
        authCode.setCode(code);
        authCode.setPurpose(purpose);
        authCode.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        repository.save(authCode);

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(email);
        message.setSubject(buildSubject(purpose));
        message.setText(buildBody(code, purpose));
        mailSender.send(message);
    }

    public void verifyCode(String email, String code, EmailAuthPurpose purpose) {
        EmailAuthCode authCode = repository
            .findTopByEmailAndPurposeAndCodeOrderByCreatedAtDesc(email, purpose, code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));

        if (authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Code expired");
        }
        if (authCode.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code already used");
        }
        authCode.setVerifiedAt(LocalDateTime.now());
        repository.save(authCode);
    }

    public void consumeVerified(String email, EmailAuthPurpose purpose) {
        EmailAuthCode authCode = repository
            .findTopByEmailAndPurposeAndVerifiedAtIsNotNullAndUsedAtIsNullOrderByVerifiedAtDesc(email, purpose)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Email not verified"));

        if (authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Code expired");
        }

        authCode.setUsedAt(LocalDateTime.now());
        repository.save(authCode);
    }

    public void verifyAndConsume(String email, String code, EmailAuthPurpose purpose) {
        EmailAuthCode authCode = repository
            .findTopByEmailAndPurposeAndCodeOrderByCreatedAtDesc(email, purpose, code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));

        if (authCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Code expired");
        }
        if (authCode.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code already used");
        }
        if (authCode.getVerifiedAt() == null) {
            authCode.setVerifiedAt(LocalDateTime.now());
        }
        authCode.setUsedAt(LocalDateTime.now());
        repository.save(authCode);
    }

    private String buildSubject(EmailAuthPurpose purpose) {
        return switch (purpose) {
            case SIGNUP -> "[OneWave] 회원가입 이메일 인증";
            case PASSWORD_RESET -> "[OneWave] 비밀번호 재설정 인증";
            case PASSWORD_CHANGE -> "[OneWave] 비밀번호 변경 인증";
        };
    }

    private String buildBody(String code, EmailAuthPurpose purpose) {
        String header = switch (purpose) {
            case SIGNUP -> "회원가입 이메일 인증 코드입니다.";
            case PASSWORD_RESET -> "비밀번호 재설정 인증 코드입니다.";
            case PASSWORD_CHANGE -> "비밀번호 변경 인증 코드입니다.";
        };
        return header + System.lineSeparator()
            + "인증 코드: " + code + System.lineSeparator()
            + "유효시간: " + expirationMinutes + "분";
    }
}
