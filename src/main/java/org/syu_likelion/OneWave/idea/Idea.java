package org.syu_likelion.OneWave.idea;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.syu_likelion.OneWave.user.User;

@Entity
@Table(name = "ideas")
@Getter
@Setter
@NoArgsConstructor
public class Idea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ideaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String problem;

    @Column(nullable = false, length = 1000)
    private String targetCustomer;

    @Column(nullable = false, length = 3000)
    private String solution;

    @Column(nullable = false, length = 2000)
    private String differentiation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdeaCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdeaStage stage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
