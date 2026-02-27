package it.asd.identity.features.updateperson;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.identity.shared.TestFixtures;
import it.asd.identity.shared.repository.PersonRepository;
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
@DisplayName("UpdatePersonHandler")
@Tag("unit")
class UpdatePersonHandlerTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UpdatePersonHandler handler;

    @Nested
    @DisplayName("when person exists and email is available")
    class WhenValid {

        @Test
        @DisplayName("returns Updated, saves entity, and publishes event")
        void returnsUpdated() {
            var personId = UUID.randomUUID();
            var cmd = TestFixtures.validUpdatePersonCommand(personId);
            var existing = TestFixtures.savedPerson(personId);
            when(personRepository.findById(personId)).thenReturn(Optional.of(existing));
            when(personRepository.findByEmail(cmd.email())).thenReturn(Optional.empty());
            when(personRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(UpdatePersonResult.Updated.class);
            verify(personRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.PERSON_UPDATED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when person does not exist")
    class WhenNotFound {

        @Test
        @DisplayName("returns NotFound without saving")
        void returnsNotFound() {
            var personId = UUID.randomUUID();
            var cmd = TestFixtures.validUpdatePersonCommand(personId);
            when(personRepository.findById(personId)).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(UpdatePersonResult.NotFound.class);
            verify(personRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when new email is already taken by another person")
    class WhenDuplicateEmail {

        @Test
        @DisplayName("returns DuplicateEmail without saving")
        void returnsDuplicateEmail() {
            var personId = UUID.randomUUID();
            var cmd = TestFixtures.updateCommandWithDuplicateEmail(personId);
            var existing = TestFixtures.savedPerson(personId);
            // existing person has a different email
            existing.setEmail("original@example.com");
            when(personRepository.findById(personId)).thenReturn(Optional.of(existing));
            when(personRepository.findByEmail(cmd.email()))
                    .thenReturn(Optional.of(TestFixtures.savedPerson(UUID.randomUUID())));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(UpdatePersonResult.DuplicateEmail.class);
            verify(personRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
