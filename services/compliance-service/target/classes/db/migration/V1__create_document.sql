-- Compliance Service schema
-- Flyway migration V1

CREATE TYPE doc_type_enum AS ENUM (
    'CERTIFICATO_MEDICO_AGONISTICO',
    'CERTIFICATO_MEDICO_NON_AGONISTICO',
    'ASSICURAZIONE',
    'TESSERA_FEDERALE',
    'CONSENSO_PRIVACY',
    'CONSENSO_IMMAGINI',
    'OTHER'
);

CREATE TYPE doc_status_enum AS ENUM (
    'VALID', 'EXPIRING_SOON', 'EXPIRED', 'MISSING', 'UNDER_REVIEW'
);

CREATE TABLE document (
    id              UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    person_id       UUID         NOT NULL,
    asd_id          UUID         NOT NULL,
    tipo            doc_type_enum NOT NULL,
    data_rilascio   DATE,
    data_scadenza   DATE,
    stato           doc_status_enum NOT NULL DEFAULT 'VALID',
    numero          VARCHAR(100),
    ente_rilascio   VARCHAR(100),
    file_url        TEXT,
    note            TEXT,
    created_at      TIMESTAMPTZ  NOT NULL    DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL    DEFAULT NOW(),

    CONSTRAINT chk_doc_dates
        CHECK (data_scadenza IS NULL
            OR data_rilascio IS NULL
            OR data_scadenza >= data_rilascio)
);

-- Index used by CheckEligibilityHandler to find active docs per person/asd/tipo
CREATE INDEX idx_doc_eligibility
    ON document (person_id, asd_id, tipo, stato)
    WHERE stato IN ('VALID', 'EXPIRING_SOON');

-- Index used by ExpiryCheckHandler daily scan
CREATE INDEX idx_doc_expiry_scan
    ON document (data_scadenza, stato)
    WHERE stato IN ('VALID', 'EXPIRING_SOON');

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_document_updated_at
    BEFORE UPDATE ON document
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
