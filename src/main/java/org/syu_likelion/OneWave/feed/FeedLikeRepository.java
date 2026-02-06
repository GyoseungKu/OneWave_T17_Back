package org.syu_likelion.OneWave.feed;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {
    long countByFeedFeedId(Long feedId);
    boolean existsByFeedFeedIdAndUserEmail(Long feedId, String email);
    Optional<FeedLike> findByFeedFeedIdAndUserEmail(Long feedId, String email);
    void deleteByFeedFeedId(Long feedId);
}
