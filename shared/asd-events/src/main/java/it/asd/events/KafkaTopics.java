package it.asd.events;

/** Central registry of all Kafka topic names. Import in both publishers and consumers. */
public final class KafkaTopics {
    private KafkaTopics() {}

    // Registry
    public static final String ASD_CREATED              = "asd.created";
    public static final String SEASON_ACTIVATED         = "season.activated";
    public static final String SEASON_CLOSED            = "season.closed";

    // Identity
    public static final String PERSON_CREATED           = "person.created";
    public static final String PERSON_UPDATED           = "person.updated";
    public static final String QUALIFICATION_ADDED      = "identity.qualification.added";

    // Membership
    public static final String MEMBERSHIP_ACTIVATED     = "membership.activated";
    public static final String GROUP_CREATED            = "membership.group.created";
    public static final String GROUP_ENROLLMENT_ADDED   = "group.enrollment.added";

    // Scheduling
    public static final String SESSION_SCHEDULED        = "scheduling.session.scheduled";

    // Compliance
    public static final String DOCUMENT_CREATED         = "compliance.document.created";
    public static final String DOCUMENT_EXPIRING        = "compliance.document.expiring_soon";
    public static final String DOCUMENT_EXPIRED         = "compliance.document.expired";
    public static final String DOCUMENT_RENEWED         = "compliance.document.renewed";
    public static final String PERSON_ELIGIBLE          = "compliance.person.eligible";
    public static final String PERSON_INELIGIBLE        = "compliance.person.ineligible";

    // Competition
    public static final String PARTICIPANT_REGISTERED   = "competition.participant.registered";
    public static final String PARTICIPANT_RESULT_SET   = "competition.participant.result_set";

    // Finance
    public static final String PAYMENT_CREATED          = "finance.payment.created";
    public static final String PAYMENT_CONFIRMED        = "finance.payment.confirmed";
    public static final String PAYMENT_OVERDUE          = "finance.payment.overdue";
}
