package it.asd.identity.shared;

import it.asd.identity.features.addqualification.AddQualificationCommand;
import it.asd.identity.features.registerperson.PersonResponse;
import it.asd.identity.features.registerperson.RegisterPersonCommand;
import it.asd.identity.features.updateperson.UpdatePersonCommand;
import it.asd.identity.shared.entity.PersonEntity;
import it.asd.identity.shared.entity.PersonStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Centralized test data for identity-service.
 * All test classes import from here — never build entities inline.
 */
public final class TestFixtures {

    private TestFixtures() {}

    // ── Commands ──────────────────────────────────────────────────────────────

    public static RegisterPersonCommand validRegisterPersonCommand() {
        return new RegisterPersonCommand(
                "RSSMRA80A01H501Z",
                "Mario",
                "Rossi",
                LocalDate.of(1980, 1, 1),
                "Roma",
                'H',
                "mario.rossi@example.com",
                "+393331234567",
                "Via Roma 1",
                "Roma",
                "RM",
                "00100"
        );
    }

    public static RegisterPersonCommand commandWithDuplicateCf() {
        return new RegisterPersonCommand(
                "RSSMRA80A01H501Z",
                "Luigi",
                "Verdi",
                null,
                null,
                'X',
                "luigi.verdi@example.com",
                null, null, null, null, null
        );
    }

    public static RegisterPersonCommand commandWithDuplicateEmail() {
        return new RegisterPersonCommand(
                "VRDLGI80A01H501Z",
                "Luigi",
                "Verdi",
                null,
                null,
                'X',
                "mario.rossi@example.com",
                null, null, null, null, null
        );
    }

    public static UpdatePersonCommand validUpdatePersonCommand(UUID personId) {
        return new UpdatePersonCommand(
                personId,
                "Mario Updated",
                null,
                "mario.updated@example.com",
                null, null, null, null, null
        );
    }

    public static UpdatePersonCommand updateCommandWithDuplicateEmail(UUID personId) {
        return new UpdatePersonCommand(
                personId,
                null,
                null,
                "taken@example.com",
                null, null, null, null, null
        );
    }

    public static AddQualificationCommand validAddQualificationCommand(UUID personId) {
        return new AddQualificationCommand(
                personId,
                "Istruttore Nuoto",
                "FIN",
                "1° Livello",
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2026, 6, 1),
                "PAT-2023-001",
                null
        );
    }

    // ── Entities (for repository stubs in unit tests) ─────────────────────────

    public static PersonEntity savedPerson(UUID id) {
        var e = PersonEntity.builder()
                .id(id)
                .codiceFiscale("RSSMRA80A01H501Z")
                .nome("Mario")
                .cognome("Rossi")
                .email("mario.rossi@example.com")
                .stato(PersonStatus.ACTIVE)
                .build();
        return e;
    }

    public static PersonEntity savedPerson() {
        return savedPerson(UUID.randomUUID());
    }

    // ── Responses ─────────────────────────────────────────────────────────────

    public static PersonResponse personResponse(UUID id) {
        return new PersonResponse(
                id, "RSSMRA80A01H501Z", "Mario", "Rossi",
                LocalDate.of(1980, 1, 1), "mario.rossi@example.com", "ACTIVE");
    }
}
