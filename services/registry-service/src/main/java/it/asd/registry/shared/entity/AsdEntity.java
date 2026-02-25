package it.asd.registry.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "asd")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsdEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codice_fiscale", nullable = false, unique = true, length = 16)
    private String codiceFiscale;

    @Column(nullable = false)
    private String nome;

    @Column(name = "codice_aff_coni")
    private String codiceAffiliazioneConi;

    @Column(name = "codice_aff_fsn")
    private String codiceAffiliazioneFsn;

    private String disciplina;
    private String citta;
    @Column(length = 100)
    private String provincia;
    private String email;
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AsdStatus stato;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
