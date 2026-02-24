package it.asd.competition.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Local read-model of compliance eligibility per (person, asd) pair.
 *
 * Warm path: populated by the async compliance.document.expired /
 *            compliance.document.renewed events.
 * Cold path: populated by a sync call to Compliance at first registration.
 *
 * If cache is absent AND the sync call fails → FAIL-CLOSED (deny registration).
 */
@Entity
@Table(name = "eligibility_cache",
       uniqueConstraints = @UniqueConstraint(columnNames = {"person_id", "asd_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EligibilityCacheEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "asd_id", nullable = false)
    private UUID asdId;

    @Column(name = "eligible", nullable = false)
    private boolean eligible;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "eligibility_blocking_docs",
                     joinColumns = @JoinColumn(name = "cache_id"))
    @Column(name = "document_type")
    @Builder.Default
    private List<String> blockingDocuments = new ArrayList<>();

    /** "sync_check" | "document.expired" | "document.renewed" */
    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @PrePersist @PreUpdate
    protected void touch() { lastUpdatedAt = LocalDateTime.now(); }
}
