package org.syu_likelion.OneWave.analysis;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
    Optional<AiAnalysis> findByIdeaIdeaId(Long ideaId);
    boolean existsByIdeaIdeaId(Long ideaId);
}
