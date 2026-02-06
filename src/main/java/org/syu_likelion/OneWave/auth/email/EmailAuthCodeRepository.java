package org.syu_likelion.OneWave.auth.email;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAuthCodeRepository extends JpaRepository<EmailAuthCode, Long> {
    Optional<EmailAuthCode> findTopByEmailAndPurposeAndCodeOrderByCreatedAtDesc(
        String email,
        EmailAuthPurpose purpose,
        String code
    );

    Optional<EmailAuthCode> findTopByEmailAndPurposeAndVerifiedAtIsNotNullAndUsedAtIsNullOrderByVerifiedAtDesc(
        String email,
        EmailAuthPurpose purpose
    );
}
