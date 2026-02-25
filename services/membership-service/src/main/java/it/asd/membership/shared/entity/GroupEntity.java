package it.asd.membership.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groups",
       uniqueConstraints = @UniqueConstraint(columnNames = {"asd_id", "season_id", "nome"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "asd_id", nullable = false)
    private UUID asdId;

    @Column(name = "season_id", nullable = false)
    private UUID seasonId;

    @Column(nullable = false)
    private String nome;

    private String disciplina;

    @Column(nullable = false)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus stato;

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
