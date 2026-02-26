package it.asd.identity.features.registerperson;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.identity.shared.TestFixtures;
import it.asd.identity.shared.entity.PersonEntity;
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
@DisplayName("RegisterPersonHandler")
@Tag("unit")
class RegisterPersonHandlerTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RegisterPersonHandler handler;

    @Nested
    @DisplayName("when input is valid and no duplicates")
    class WhenValid {

        @Test
        @DisplayName("returns Registered, saves entity, and publishes event")
        void returnsRegistered() {
            var cmd = TestFixtures.validRegisterPersonCommand();
            when(personRepository.findByCodiceFiscale(cmd.codiceFiscale())).thenReturn(Optional.empty());
            when(personRepository.findByEmail(cmd.email())).thenReturn(Optional.empty());
            when(personRepository.save(any())).thenAnswer(inv -> {
                PersonEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterPersonResult.Registered.class);
            verify(personRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.PERSON_CREATED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when codice fiscale already exists")
    class WhenDuplicateCf {

        @Test
        @DisplayName("returns DuplicateCodiceFiscale without saving")
        void returnsDuplicateCf() {
            var cmd = TestFixtures.commandWithDuplicateCf();
            when(personRepository.findByCodiceFiscale(cmd.codiceFiscale()))
                    .thenReturn(Optional.of(TestFixtures.savedPerson()));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterPersonResult.DuplicateCodiceFiscale.class);
            verify(personRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when email already exists")
    class WhenDuplicateEmail {

        @Test
        @DisplayName("returns DuplicateEmail without saving")
        void returnsDuplicateEmail() {
            var cmd = TestFixtures.commandWithDuplicateEmail();
            when(personRepository.findByCodiceFiscale(cmd.codiceFiscale())).thenReturn(Optional.empty());
            when(personRepository.findByEmail(cmd.email()))
                    .thenReturn(Optional.of(TestFixtures.savedPerson()));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterPersonResult.DuplicateEmail.class);
            verify(personRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
