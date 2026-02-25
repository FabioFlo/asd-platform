CREATE TYPE asd_status_enum    AS ENUM ('ACTIVE', 'SUSPENDED', 'DISSOLVED');
CREATE TYPE season_status_enum AS ENUM ('PLANNED', 'ACTIVE', 'CLOSED');

CREATE TABLE asd (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codice_fiscale      VARCHAR(16)  NOT NULL UNIQUE,
    nome                VARCHAR(255) NOT NULL,
    codice_aff_coni     VARCHAR(50),
    codice_aff_fsn      VARCHAR(50),
    disciplina          VARCHAR(100),
    citta               VARCHAR(100),
    provincia           VARCHAR(100),
    email               VARCHAR(255),
    telefono            VARCHAR(20),
    stato               asd_status_enum NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE season (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asd_id      UUID         NOT NULL,
    codice      VARCHAR(10)  NOT NULL,
    data_inizio DATE         NOT NULL,
    data_fine   DATE         NOT NULL,
    stato       season_status_enum NOT NULL DEFAULT 'PLANNED',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_season_asd_codice UNIQUE (asd_id, codice),
    CONSTRAINT chk_season_dates     CHECK  (data_fine > data_inizio)
);

-- Enforces the invariant: only one ACTIVE season per ASD at DB level
CREATE UNIQUE INDEX uq_one_active_season
    ON season (asd_id)
    WHERE stato = 'ACTIVE';

CREATE INDEX idx_season_asd_stato ON season (asd_id, stato);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_asd_updated_at
    BEFORE UPDATE ON asd FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_season_updated_at
    BEFORE UPDATE ON season FOR EACH ROW EXECUTE FUNCTION set_updated_at();
