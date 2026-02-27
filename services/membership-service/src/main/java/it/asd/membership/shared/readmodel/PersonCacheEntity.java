package it.asd.membership.shared.readmodel;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "person_cache",
        uniqueConstraints = @UniqueConstraint(columnNames = {"person_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonCacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "person_id", nullable = false, unique = true)
    private UUID personId;

    private String nome;
    private String cognome;

    @Column(name = "codice_fiscale")
    private String codiceFiscale;

    private String email;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Column(nullable = false)
    private String source;
}
