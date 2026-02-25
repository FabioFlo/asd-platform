CREATE TYPE payment_type_enum   AS ENUM ('QUOTA_ASSOCIATIVA','QUOTA_CORSO','ISCRIZIONE_GARA','ALTRO');
CREATE TYPE payment_status_enum AS ENUM ('PENDING','CONFIRMED','OVERDUE','REFUNDED','CANCELLED');

CREATE TABLE payment (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id         UUID           NOT NULL,
    asd_id            UUID           NOT NULL,
    season_id         UUID,
    trigger_event_id  UUID           UNIQUE,
    trigger_type      VARCHAR(80),
    payment_type      payment_type_enum NOT NULL,
    importo           NUMERIC(10,2)  NOT NULL,
    data_scadenza     DATE,
    data_pagamento    DATE,
    stato             payment_status_enum NOT NULL DEFAULT 'PENDING',
    metodo_pagamento  VARCHAR(50),
    riferimento       VARCHAR(100),
    note              TEXT,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE fee_rule (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asd_id            UUID              NOT NULL,
    season_id         UUID              NOT NULL,
    payment_type      payment_type_enum NOT NULL,
    importo           NUMERIC(10,2)     NOT NULL,
    giorni_scadenza   INT               NOT NULL DEFAULT 30,
    attivo            BOOLEAN           NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fee_rule UNIQUE (asd_id, season_id, payment_type)
);

CREATE INDEX idx_payment_overdue_scan
    ON payment (stato, data_scadenza)
    WHERE stato = 'PENDING';

CREATE INDEX idx_payment_person ON payment (person_id, asd_id, stato);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_payment_updated_at
    BEFORE UPDATE ON payment FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_fee_rule_updated_at
    BEFORE UPDATE ON fee_rule FOR EACH ROW EXECUTE FUNCTION set_updated_at();
