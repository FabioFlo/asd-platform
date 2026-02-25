package it.asd.registry.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "season",
       uniqueConstraints = @UniqueConstraint(columnNames = {"asd_id", "codice"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeasonEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "asd_id", nullable = false, updatable = false)
    private UUID asdId;

    @Column(nullable = false, length = 10)
    private String codice;

    @Column(name = "data_inizio", nullable = false)
    private LocalDate dataInizio;

    @Column(name = "data_fine", nullable = false)
    private LocalDate dataFine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeasonStatus stato;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
