package it.asd.common.enums;

public enum AsdRole {

    // Membership / organizational roles (used in role_assignment table)
    ATLETA,          // registered athlete / member
    TECNICO,         // coach / trainer
    DIRIGENTE,       // club director / board member
    AMMINISTRATORE,  // administrative staff

    // Group-level roles (used in group_enrollment table)
    CAPITANO,        // team captain
    VICE_CAPITANO    // vice-captain
}
