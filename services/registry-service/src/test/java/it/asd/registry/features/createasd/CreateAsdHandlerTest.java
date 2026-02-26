package it.asd.registry.features.createasd;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.registry.shared.TestFixtures;
import it.asd.registry.shared.entity.AsdEntity;
import it.asd.registry.shared.repository.AsdRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAsdHandler")
@Tag("unit")
class CreateAsdHandlerTest {

    @Mock
    private AsdRepository asdRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CreateAsdHandler handler;

    @Nested
    @DisplayName("when codice fiscale is new")
    class WhenValid {

        @Test
        @DisplayName("returns Created, saves entity, and publishes event")
        void returnsCreated() {
            var cmd = TestFixtures.validCreateAsdCommand();
            when(asdRepository.findByCodiceFiscale(cmd.codiceFiscale())).thenReturn(Optional.empty());
            when(asdRepository.save(any())).thenAnswer(inv -> {
                AsdEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(CreateAsdResult.Created.class);
            verify(asdRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.ASD_CREATED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when codice fiscale already exists")
    class WhenDuplicateCf {

        @Test
        @DisplayName("returns DuplicateCodiceFiscale without saving")
        void returnsDuplicate() {
            var cmd = TestFixtures.commandWithDuplicateCf();
            when(asdRepository.findByCodiceFiscale(cmd.codiceFiscale()))
                    .thenReturn(Optional.of(TestFixtures.savedAsd()));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(CreateAsdResult.DuplicateCodiceFiscale.class);
            verify(asdRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
