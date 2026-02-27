package it.asd.identity.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "qualification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "person_id", nullable = false, updatable = false)
    private UUID personId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String ente;

    @Column(nullable = false)
    private String livello;

    @Column(name = "data_conseguimento")
    private LocalDate dataConseguimento;

    @Column(name = "data_scadenza")
    private LocalDate dataScadenza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualificationStatus stato;

    @Column(name = "numero_patentino")
    private String numeroPatentino;

    private String note;

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
