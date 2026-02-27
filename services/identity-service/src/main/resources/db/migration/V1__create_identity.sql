-- Identity Service schema
-- Flyway migration V1

CREATE TABLE person
(
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codice_fiscale           VARCHAR(16)  NOT NULL UNIQUE,
    nome                     VARCHAR(100) NOT NULL,
    cognome                  VARCHAR(100) NOT NULL,
    data_nascita             DATE,
    luogo_nascita            VARCHAR(100),
    codice_provincia_nascita CHAR(2),
    email                    VARCHAR(255) UNIQUE,
    telefono                 VARCHAR(20),
    indirizzo                VARCHAR(255),
    citta                    VARCHAR(100),
    provincia                VARCHAR(100),
    cap                      VARCHAR(10),
    stato                    VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE qualification
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id          UUID         NOT NULL,
    tipo               VARCHAR(80)  NOT NULL,
    ente               VARCHAR(80)  NOT NULL,
    livello            VARCHAR(50)  NOT NULL,
    data_conseguimento DATE,
    data_scadenza      DATE,
    stato              VARCHAR(20)  NOT NULL DEFAULT 'VALID',
    numero_patentino   VARCHAR(100),
    note               TEXT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_person_cf ON person (codice_fiscale);
CREATE INDEX idx_qual_person ON qualification (person_id, tipo, stato);

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_person_updated_at
    BEFORE UPDATE
    ON person
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_qualification_updated_at
    BEFORE UPDATE
    ON qualification
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
