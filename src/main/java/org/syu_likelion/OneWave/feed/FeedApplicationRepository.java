package org.syu_likelion.OneWave.feed;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedApplicationRepository extends JpaRepository<FeedApplication, Long> {
    Optional<FeedApplication> findByFeedFeedIdAndUserEmail(Long feedId, String email);
    Optional<FeedApplication> findByApplicationIdAndFeedFeedId(Long applicationId, Long feedId);
    long countByFeedFeedIdAndStatusAndStack(Long feedId, FeedApplicationStatus status, String stack);
    long countByFeedFeedIdAndStatus(Long feedId, FeedApplicationStatus status);

    @Query("""
        select a
        from FeedApplication a
        join fetch a.user u
        where a.feed.feedId = :feedId
        order by a.createdAt desc
    """)
    List<FeedApplication> findAllByFeedIdWithUser(Long feedId);

    @Query("""
        select a
        from FeedApplication a
        join fetch a.feed f
        join fetch f.idea i
        where a.user.email = :email
        order by a.createdAt desc
    """)
    List<FeedApplication> findAllByUserEmailWithFeed(String email);

    @Query("""
        select a
        from FeedApplication a
        join fetch a.feed f
        join fetch f.idea i
        join fetch f.user u
        where a.user.email = :email
        and a.status = 'APPROVED'
        order by a.updatedAt desc
    """)
    List<FeedApplication> findApprovedByUserEmailWithFeed(String email);

    @Query("""
        select a
        from FeedApplication a
        join fetch a.user u
        where a.feed.feedId = :feedId
        and a.status = 'APPROVED'
        order by a.updatedAt desc
    """)
    List<FeedApplication> findApprovedByFeedIdWithUser(Long feedId);

    void deleteByFeedFeedId(Long feedId);
}
