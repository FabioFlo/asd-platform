package it.asd.scheduling.shared.repository;

import it.asd.scheduling.shared.readmodel.GroupCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupCacheRepository extends JpaRepository<GroupCacheEntity, UUID> {
    Optional<GroupCacheEntity> findByGroupId(UUID groupId);
}
