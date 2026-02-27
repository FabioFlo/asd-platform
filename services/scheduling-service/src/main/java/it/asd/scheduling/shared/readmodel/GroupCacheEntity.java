package it.asd.scheduling.shared.readmodel;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_cache",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id", nullable = false, unique = true)
    private UUID groupId;

    @Column(name = "asd_id", nullable = false)
    private UUID asdId;

    @Column(name = "season_id", nullable = false)
    private UUID seasonId;

    @Column(nullable = false)
    private String nome;

    private String disciplina;
    private String tipo;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;
}
