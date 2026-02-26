package it.asd.membership.features.creategroup;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.membership.shared.TestFixtures;
import it.asd.membership.shared.entity.GroupEntity;
import it.asd.membership.shared.repository.GroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateGroupHandler")
@Tag("unit")
class CreateGroupHandlerTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CreateGroupHandler handler;

    @Nested
    @DisplayName("when group name is unique for this asd and season")
    class WhenValid {

        @Test
        @DisplayName("returns Created, saves entity, and publishes membership.group.created event")
        void returnsCreated() {
            var asdId = UUID.randomUUID();
            var cmd = TestFixtures.validCreateGroupCommand(asdId);

            when(groupRepository.existsByAsdIdAndSeasonIdAndNome(cmd.asdId(), cmd.seasonId(), cmd.nome()))
                    .thenReturn(false);

            when(groupRepository.save(any())).thenAnswer(inv -> {
                GroupEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(CreateGroupResult.Created.class);
            var created = (CreateGroupResult.Created) result;
            assertThat(created.groupId()).isNotNull();

            verify(groupRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.GROUP_CREATED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when a group with the same name already exists for this asd and season")
    class WhenDuplicateName {

        @Test
        @DisplayName("returns DuplicateName without saving or publishing")
        void returnsDuplicateName() {
            var asdId = UUID.randomUUID();
            var cmd = TestFixtures.validCreateGroupCommand(asdId);

            when(groupRepository.existsByAsdIdAndSeasonIdAndNome(cmd.asdId(), cmd.seasonId(), cmd.nome()))
                    .thenReturn(true);

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(CreateGroupResult.DuplicateName.class);
            var duplicate = (CreateGroupResult.DuplicateName) result;
            assertThat(duplicate.nome()).isEqualTo(cmd.nome());

            verify(groupRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
