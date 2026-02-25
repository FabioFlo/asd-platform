package it.asd.registry.features.getcurrentseason;

import it.asd.registry.shared.entity.SeasonStatus;
import it.asd.registry.shared.repository.SeasonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetCurrentSeasonHandler {

    private final SeasonRepository seasonRepository;

    public GetCurrentSeasonHandler(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Transactional(readOnly = true)
    public GetCurrentSeasonResult handle(GetCurrentSeasonQuery query) {
        return seasonRepository.findByAsdIdAndStato(query.asdId(), SeasonStatus.ACTIVE)
                .map(s -> (GetCurrentSeasonResult) new GetCurrentSeasonResult.Found(
                        s.getId(), s.getCodice(), s.getDataInizio(), s.getDataFine()))
                .orElseGet(() -> new GetCurrentSeasonResult.NoActiveSeason(query.asdId()));
    }
}
