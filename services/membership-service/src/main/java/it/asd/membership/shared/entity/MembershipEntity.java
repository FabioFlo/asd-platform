package it.asd.membership.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "membership",
       uniqueConstraints = @UniqueConstraint(columnNames = {"person_id", "asd_id", "season_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MembershipEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "asd_id", nullable = false)
    private UUID asdId;

    @Column(name = "season_id", nullable = false)
    private UUID seasonId;

    @Column(name = "numero_tessera", nullable = false)
    private String numeroTessera;

    @Column(name = "data_iscrizione", nullable = false)
    private LocalDate dataIscrizione;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus stato;

    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
