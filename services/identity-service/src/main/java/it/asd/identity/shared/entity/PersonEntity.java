package it.asd.identity.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "person")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "codice_fiscale", nullable = false, unique = true, length = 16)
    private String codiceFiscale;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cognome;

    @Column(name = "data_nascita")
    private LocalDate dataNascita;

    @Column(name = "luogo_nascita")
    private String luogoNascita;

    @Column(name = "codice_provincia_nascita")
    private char codiceProvinciaNascita;

    @Column(unique = true)
    private String email;

    private String telefono;
    private String indirizzo;
    private String citta;
    private String provincia;
    private String cap;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PersonStatus stato;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
