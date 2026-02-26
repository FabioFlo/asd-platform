package it.asd.competition.features.recordresult;

import it.asd.competition.shared.entity.EventParticipationEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-26T10:47:44+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class ParticipationMapperImpl implements ParticipationMapper {

    @Override
    public void updateFromCommand(RecordResultCommand cmd, EventParticipationEntity entity) {
        if ( cmd == null ) {
            return;
        }

        if ( cmd.posizione() != null ) {
            entity.setPosizione( cmd.posizione() );
        }
        if ( cmd.punteggio() != null ) {
            entity.setPunteggio( cmd.punteggio() );
        }
        if ( entity.getResultData() != null ) {
            Map<String, Object> map = cmd.resultData();
            if ( map != null ) {
                entity.getResultData().clear();
                entity.getResultData().putAll( map );
            }
        }
        else {
            Map<String, Object> map = cmd.resultData();
            if ( map != null ) {
                entity.setResultData( new LinkedHashMap<String, Object>( map ) );
            }
        }
    }
}
