package it.asd.common.exception;

/**
 * Thrown when a domain invariant is violated.
 * 'rule' is a stable string identifier usable by clients for i18n.
 */
public final class BusinessRuleViolationException extends RuntimeException {
    private final String rule;

    public BusinessRuleViolationException(String rule, String message) {
        super(message);
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }
}
