package org.syu_likelion.OneWave.feed;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedRepository extends JpaRepository<Feed, Long> {
    Optional<Feed> findByFeedIdAndUserEmail(Long feedId, String email);
    Optional<Feed> findByIdeaIdeaId(Long ideaId);

    @Query("""
        select f
        from Feed f
        join fetch f.idea i
        join fetch f.user u
        where f.feedId = :feedId
    """)
    Optional<Feed> findDetailByFeedId(Long feedId);

    @Query("""
        select f
        from Feed f
        join fetch f.idea i
        join fetch f.user u
        order by f.createdAt desc
    """)
    List<Feed> findAllWithIdeaAndUser();
}
