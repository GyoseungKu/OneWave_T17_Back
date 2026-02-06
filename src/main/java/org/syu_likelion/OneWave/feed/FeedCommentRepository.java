package org.syu_likelion.OneWave.feed;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {
    long countByFeedFeedId(Long feedId);
    @Query("""
        select c
        from FeedComment c
        join fetch c.user u
        where c.feed.feedId = :feedId
        order by c.createdAt asc
    """)
    List<FeedComment> findAllByFeedFeedIdOrderByCreatedAtAsc(Long feedId);
    void deleteByFeedFeedId(Long feedId);
    java.util.Optional<FeedComment> findByFeedCommentIdAndUserEmail(Long feedCommentId, String email);
}
