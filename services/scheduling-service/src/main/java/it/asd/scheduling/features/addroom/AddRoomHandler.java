package it.asd.scheduling.features.addroom;

import it.asd.scheduling.shared.entity.RoomEntity;
import it.asd.scheduling.shared.entity.RoomStatus;
import it.asd.scheduling.shared.repository.RoomRepository;
import it.asd.scheduling.shared.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AddRoomHandler {

    private static final Logger log = LoggerFactory.getLogger(AddRoomHandler.class);

    private final VenueRepository venueRepository;
    private final RoomRepository roomRepository;

    public AddRoomHandler(VenueRepository venueRepository, RoomRepository roomRepository) {
        this.venueRepository = venueRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public AddRoomResult handle(AddRoomCommand cmd) {
        if (venueRepository.findById(cmd.venueId()).isEmpty()) {
            return new AddRoomResult.VenueNotFound(cmd.venueId());
        }

        if (roomRepository.existsByVenueIdAndNome(cmd.venueId(), cmd.nome())) {
            return new AddRoomResult.DuplicateName(cmd.nome());
        }

        var entity = RoomEntity.builder()
                .venueId(cmd.venueId())
                .nome(cmd.nome())
                .capienza(cmd.capienza())
                .stato(RoomStatus.ACTIVE)
                .note(cmd.note())
                .build();

        var saved = roomRepository.save(entity);
        log.info("[ADD_ROOM] Saved roomId={} venueId={}", saved.getId(), saved.getVenueId());
        return new AddRoomResult.Added(saved.getId());
    }
}
