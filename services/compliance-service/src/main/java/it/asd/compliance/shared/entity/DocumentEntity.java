package it.asd.compliance.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lombok IS justified here: JPA requires a no-arg constructor and mutable
 * fields — both incompatible with records. This is the ONLY Lombok usage
 * in this service. All feature types (commands, results, responses) are records.
 */
@Entity @Table(name = "document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "person_id", nullable = false, updatable = false)
    private UUID personId;

    @Column(name = "asd_id", nullable = false, updatable = false)
    private UUID asdId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, updatable = false)
    private DocumentType tipo;

    @Column(name = "data_rilascio") private LocalDate dataRilascio;
    @Column(name = "data_scadenza") private LocalDate dataScadenza;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false)
    private DocumentStatus stato;

    @Column(name = "numero")       private String numero;
    @Column(name = "ente_rilascio") private String enteRilascio;
    @Column(name = "file_url")     private String fileUrl;
    @Column(name = "note")         private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist  protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate   protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public boolean isExpired() {
        return dataScadenza != null && dataScadenza.isBefore(LocalDate.now());
    }
    public boolean isExpiringSoon(int days) {
        return dataScadenza != null && !isExpired()
                && dataScadenza.isBefore(LocalDate.now().plusDays(days));
    }
}
