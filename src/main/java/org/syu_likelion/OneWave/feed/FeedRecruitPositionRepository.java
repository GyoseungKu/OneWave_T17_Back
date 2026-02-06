package org.syu_likelion.OneWave.feed;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRecruitPositionRepository extends JpaRepository<FeedRecruitPosition, Long> {
    List<FeedRecruitPosition> findAllByFeedFeedId(Long feedId);
    void deleteByFeedFeedId(Long feedId);
}
