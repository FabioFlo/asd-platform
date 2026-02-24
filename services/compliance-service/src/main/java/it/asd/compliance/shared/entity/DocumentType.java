package it.asd.compliance.shared.entity;

import java.util.Set;

public enum DocumentType {
    CERTIFICATO_MEDICO_AGONISTICO,
    CERTIFICATO_MEDICO_NON_AGONISTICO,
    ASSICURAZIONE,
    TESSERA_FEDERALE,
    CONSENSO_PRIVACY,
    CONSENSO_IMMAGINI,
    OTHER;

    public static final Set<DocumentType> AGONISTIC_REQUIRED =
            Set.of(CERTIFICATO_MEDICO_AGONISTICO, ASSICURAZIONE, TESSERA_FEDERALE);

    public static final Set<DocumentType> RECREATIONAL_REQUIRED =
            Set.of(CERTIFICATO_MEDICO_NON_AGONISTICO, ASSICURAZIONE);
}
