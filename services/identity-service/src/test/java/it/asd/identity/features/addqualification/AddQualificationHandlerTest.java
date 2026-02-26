package it.asd.identity.features.addqualification;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.identity.shared.TestFixtures;
import it.asd.identity.shared.entity.QualificationEntity;
import it.asd.identity.shared.repository.PersonRepository;
import it.asd.identity.shared.repository.QualificationRepository;
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
@DisplayName("AddQualificationHandler")
@Tag("unit")
class AddQualificationHandlerTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private QualificationRepository qualificationRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private AddQualificationHandler handler;

    @Nested
    @DisplayName("when person exists")
    class WhenPersonExists {

        @Test
        @DisplayName("returns Added, saves qualification, and publishes event")
        void returnsAdded() {
            var personId = UUID.randomUUID();
            var cmd = TestFixtures.validAddQualificationCommand(personId);
            when(personRepository.findById(personId)).thenReturn(Optional.of(TestFixtures.savedPerson(personId)));
            when(qualificationRepository.save(any())).thenAnswer(inv -> {
                QualificationEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddQualificationResult.Added.class);
            verify(qualificationRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.QUALIFICATION_ADDED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when person does not exist")
    class WhenPersonNotFound {

        @Test
        @DisplayName("returns PersonNotFound without saving")
        void returnsPersonNotFound() {
            var personId = UUID.randomUUID();
            var cmd = TestFixtures.validAddQualificationCommand(personId);
            when(personRepository.findById(personId)).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddQualificationResult.PersonNotFound.class);
            verify(qualificationRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
