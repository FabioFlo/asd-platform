-- Scheduling Service schema
-- Flyway migration V1

CREATE TABLE venue
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asd_id     UUID         NOT NULL,
    nome       VARCHAR(100) NOT NULL,
    indirizzo  VARCHAR(255),
    citta      VARCHAR(100),
    provincia  VARCHAR(100),
    stato      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    note       TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE room
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venue_id   UUID         NOT NULL REFERENCES venue (id),
    nome       VARCHAR(100) NOT NULL,
    capienza   INT,
    stato      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    note       TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE session
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asd_id     UUID         NOT NULL,
    venue_id   UUID         NOT NULL REFERENCES venue (id),
    room_id    UUID REFERENCES room (id),
    group_id   UUID,
    titolo     VARCHAR(200) NOT NULL,
    data       DATE         NOT NULL,
    ora_inizio TIME         NOT NULL,
    ora_fine   TIME         NOT NULL,
    tipo       VARCHAR(20)  NOT NULL,
    stato      VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    note       TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_session_times CHECK (ora_fine > ora_inizio)
);

CREATE TABLE group_cache
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    group_id       UUID         NOT NULL UNIQUE,
    asd_id         UUID         NOT NULL,
    season_id      UUID         NOT NULL,
    nome           VARCHAR(100) NOT NULL,
    disciplina     VARCHAR(100),
    tipo           VARCHAR(50),
    last_synced_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Overlap detection: find sessions in same room on same day
CREATE INDEX idx_session_room_date
    ON session (room_id, data)
    WHERE room_id IS NOT NULL AND stato = 'SCHEDULED';

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_venue_updated_at
    BEFORE UPDATE
    ON venue
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_room_updated_at
    BEFORE UPDATE
    ON room
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_session_updated_at
    BEFORE UPDATE
    ON session
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
