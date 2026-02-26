package it.asd.identity.features.getperson;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPersonHandler")
@Tag("unit")
class GetPersonHandlerTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private GetPersonHandler handler;

    @Nested
    @DisplayName("when person exists")
    class WhenFound {

        @Test
        @DisplayName("returns Found with person response")
        void returnsFound() {
            var personId = UUID.randomUUID();
            when(personRepository.findById(personId)).thenReturn(Optional.of(TestFixtures.savedPerson(personId)));

            var result = handler.handle(new GetPersonQuery(personId));

            assertThat(result).isInstanceOf(GetPersonResult.Found.class);
            var found = (GetPersonResult.Found) result;
            assertThat(found.response().id()).isEqualTo(personId);
            assertThat(found.response().codiceFiscale()).isEqualTo("RSSMRA80A01H501Z");
        }
    }

    @Nested
    @DisplayName("when person does not exist")
    class WhenNotFound {

        @Test
        @DisplayName("returns NotFound")
        void returnsNotFound() {
            var personId = UUID.randomUUID();
            when(personRepository.findById(personId)).thenReturn(Optional.empty());

            var result = handler.handle(new GetPersonQuery(personId));

            assertThat(result).isInstanceOf(GetPersonResult.NotFound.class);
            assertThat(((GetPersonResult.NotFound) result).personId()).isEqualTo(personId);
        }
    }
}
