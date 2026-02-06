package org.syu_likelion.OneWave.idea;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    Optional<Idea> findByIdeaIdAndUserEmail(Long ideaId, String email);

    List<Idea> findAllByUserEmailOrderByCreatedAtDesc(String email);
}
