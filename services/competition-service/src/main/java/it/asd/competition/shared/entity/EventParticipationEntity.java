package it.asd.competition.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Lombok justified: JPA no-arg constructor + mutable fields required.
 * All feature types (commands, results, responses) are records.
 */
@Entity
@Table(name = "event_participation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventParticipationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "asd_id", nullable = false)
    private UUID asdId;

    @Column(name = "season_id")
    private UUID seasonId;

    @Column(name = "categoria")
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false)
    private ParticipationStatus stato;

    @Column(name = "posizione")
    private Integer posizione;

    @Column(name = "punteggio", precision = 10, scale = 2)
    private BigDecimal punteggio;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_data", columnDefinition = "jsonb")
    private Map<String, Object> resultData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
