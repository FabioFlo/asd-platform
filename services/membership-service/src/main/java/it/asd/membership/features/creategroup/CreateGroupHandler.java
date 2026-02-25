package it.asd.membership.features.creategroup;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.membership.GroupCreatedEvent;
import it.asd.membership.shared.entity.GroupEntity;
import it.asd.membership.shared.entity.GroupStatus;
import it.asd.membership.shared.repository.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class CreateGroupHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateGroupHandler.class);

    private final GroupRepository groupRepository;
    private final EventPublisher eventPublisher;

    public CreateGroupHandler(GroupRepository groupRepository, EventPublisher eventPublisher) {
        this.groupRepository = groupRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CreateGroupResult handle(CreateGroupCommand cmd) {
        if (groupRepository.existsByAsdIdAndSeasonIdAndNome(cmd.asdId(), cmd.seasonId(), cmd.nome())) {
            return new CreateGroupResult.DuplicateName(cmd.nome());
        }

        var entity = GroupEntity.builder()
                .asdId(cmd.asdId())
                .seasonId(cmd.seasonId())
                .nome(cmd.nome())
                .disciplina(cmd.disciplina())
                .tipo(cmd.tipo())
                .stato(GroupStatus.ACTIVE)
                .note(cmd.note())
                .build();

        var saved = groupRepository.save(entity);
        log.info("[CREATE_GROUP] Saved groupId={} nome={}", saved.getId(), saved.getNome());

        eventPublisher.publish(
                KafkaTopics.GROUP_CREATED,
                new GroupCreatedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getAsdId(), saved.getSeasonId(),
                        saved.getNome(), saved.getDisciplina(), saved.getTipo(), Instant.now()),
                saved.getAsdId(), saved.getSeasonId());

        return new CreateGroupResult.Created(saved.getId());
    }
}
