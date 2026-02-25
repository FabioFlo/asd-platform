package it.asd.scheduling.features.createvenue;

import it.asd.scheduling.shared.entity.VenueEntity;
import it.asd.scheduling.shared.entity.VenueStatus;
import it.asd.scheduling.shared.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateVenueHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateVenueHandler.class);

    private final VenueRepository venueRepository;

    public CreateVenueHandler(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    public CreateVenueResult handle(CreateVenueCommand cmd) {
        if (venueRepository.existsByAsdIdAndNome(cmd.asdId(), cmd.nome())) {
            return new CreateVenueResult.DuplicateName(cmd.nome());
        }

        var entity = VenueEntity.builder()
                .asdId(cmd.asdId())
                .nome(cmd.nome())
                .indirizzo(cmd.indirizzo())
                .citta(cmd.citta())
                .provincia(cmd.provincia())
                .stato(VenueStatus.ACTIVE)
                .note(cmd.note())
                .build();

        var saved = venueRepository.save(entity);
        log.info("[CREATE_VENUE] Saved venueId={} nome={}", saved.getId(), saved.getNome());
        return new CreateVenueResult.Created(saved.getId());
    }
}
