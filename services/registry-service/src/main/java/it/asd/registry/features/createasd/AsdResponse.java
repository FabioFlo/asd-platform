package it.asd.registry.features.createasd;

import it.asd.registry.shared.entity.AsdEntity;

import java.util.UUID;

public record AsdResponse(
        UUID id,
        String codiceFiscale,
        String nome,
        String disciplina,
        String stato
) {
    public static AsdResponse from(AsdEntity e) {
        return new AsdResponse(e.getId(), e.getCodiceFiscale(), e.getNome(),
                e.getDisciplina(), e.getStato().name());
    }
}
