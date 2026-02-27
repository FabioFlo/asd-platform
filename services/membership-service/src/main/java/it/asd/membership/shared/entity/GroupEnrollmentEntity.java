package it.asd.membership.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_enrollment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"person_id", "group_id", "season_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupEnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "asd_id", nullable = false)
    private UUID asdId;

    @Column(name = "season_id", nullable = false)
    private UUID seasonId;

    @Column(nullable = false)
    private String ruolo;

    @Column(name = "data_ingresso", nullable = false)
    private LocalDate dataIngresso;

    @Column(name = "data_uscita")
    private LocalDate dataUscita;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus stato;

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
