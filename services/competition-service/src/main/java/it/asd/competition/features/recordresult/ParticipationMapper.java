package it.asd.competition.features.recordresult;

import it.asd.competition.shared.entity.EventParticipationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for the RecordResult feature.
 * <p>
 * WHY MapStruct here and not a static factory?
 * 1. resultData is Map<String,Object> — MapStruct handles null-safe deep copy
 * of the map without manual null checks
 * 2. We need to update an existing entity (MappingTarget) rather than create new
 * 3. posizione/punteggio may be null and MapStruct respects NullValuePropertyMappingStrategy
 * <p>
 * componentModel = SPRING: MapStruct generates a @Component — injectable via constructor.
 * unmappedTargetPolicy = ERROR: compile fails if we add a field and forget the mapping.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy =
                org.mapstruct.NullValuePropertyMappingStrategy.IGNORE   // don't overwrite with null
)
public interface ParticipationMapper {

    /**
     * Updates an existing entity with result data from the command.
     * Fields not in the command (eventId, personId, stato, etc.) are untouched.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "personId", ignore = true)
    @Mapping(target = "groupId", ignore = true)
    @Mapping(target = "asdId", ignore = true)
    @Mapping(target = "seasonId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "stato", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromCommand(RecordResultCommand cmd,
                           @MappingTarget EventParticipationEntity entity);
}
