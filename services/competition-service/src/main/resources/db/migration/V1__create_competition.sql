-- Competition Service schema
-- Flyway migration V1

CREATE TABLE event_participation
(
    id          UUID PRIMARY KEY  NOT NULL DEFAULT gen_random_uuid(),
    event_id    UUID              NOT NULL,
    person_id   UUID,
    group_id    UUID,
    asd_id      UUID              NOT NULL,
    season_id   UUID,
    categoria   VARCHAR(50),
    stato       VARCHAR(20)       NOT NULL DEFAULT 'REGISTERED',
    posizione   INT,
    punteggio   NUMERIC(10, 2),
    result_data JSONB,
    note        TEXT,
    created_at  TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_has_participant
        CHECK (person_id IS NOT NULL OR group_id IS NOT NULL)
);

CREATE INDEX idx_participation_person_event
    ON event_participation (person_id, event_id) WHERE person_id IS NOT NULL;

-- GIN index for sport-specific result_data queries (satellite services)
CREATE INDEX idx_participation_result_gin
    ON event_participation USING GIN (result_data);

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_participation_updated_at
    BEFORE UPDATE
    ON event_participation
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Eligibility cache: local read-model updated by compliance events
CREATE TABLE eligibility_cache
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    person_id       UUID        NOT NULL,
    asd_id          UUID        NOT NULL,
    eligible        BOOLEAN     NOT NULL DEFAULT FALSE,
    source          VARCHAR(50) NOT NULL DEFAULT 'sync_check',
    last_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_eligibility UNIQUE (person_id, asd_id)
);

CREATE TABLE eligibility_blocking_docs
(
    cache_id      UUID         NOT NULL
        REFERENCES eligibility_cache (id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL
);

CREATE INDEX idx_eligibility_lookup
    ON eligibility_cache (person_id, asd_id);
