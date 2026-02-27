CREATE TYPE membership_status_enum AS ENUM ('PENDING','ACTIVE','SUSPENDED','EXPIRED');
CREATE TYPE role_status_enum AS ENUM ('ACTIVE','REVOKED');
CREATE TYPE group_status_enum AS ENUM ('ACTIVE','ARCHIVED');
CREATE TYPE enrollment_status_enum AS ENUM ('ACTIVE','WITHDRAWN');

CREATE TABLE membership
(
    id              UUID PRIMARY KEY                DEFAULT gen_random_uuid(),
    person_id       UUID                   NOT NULL,
    asd_id          UUID                   NOT NULL,
    season_id       UUID                   NOT NULL,
    numero_tessera  VARCHAR(50)            NOT NULL,
    data_iscrizione DATE                   NOT NULL,
    stato           membership_status_enum NOT NULL DEFAULT 'ACTIVE',
    note            TEXT,
    created_at      TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_membership UNIQUE (person_id, asd_id, season_id)
);

CREATE TABLE role_assignment
(
    id            UUID PRIMARY KEY          DEFAULT gen_random_uuid(),
    membership_id UUID             NOT NULL REFERENCES membership (id),
    asd_id        UUID             NOT NULL,
    season_id     UUID             NOT NULL,
    ruolo         VARCHAR(80)      NOT NULL,
    data_inizio   DATE             NOT NULL,
    data_fine     DATE,
    stato         role_status_enum NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

CREATE TABLE groups
(
    id         UUID PRIMARY KEY           DEFAULT gen_random_uuid(),
    asd_id     UUID              NOT NULL,
    season_id  UUID              NOT NULL,
    nome       VARCHAR(100)      NOT NULL,
    disciplina VARCHAR(100),
    tipo       VARCHAR(50)       NOT NULL,
    stato      group_status_enum NOT NULL DEFAULT 'ACTIVE',
    note       TEXT,
    created_at TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_group_name UNIQUE (asd_id, season_id, nome)
);

CREATE TABLE group_enrollment
(
    id            UUID PRIMARY KEY                DEFAULT gen_random_uuid(),
    person_id     UUID                   NOT NULL,
    group_id      UUID                   NOT NULL REFERENCES groups (id),
    asd_id        UUID                   NOT NULL,
    season_id     UUID                   NOT NULL,
    ruolo         VARCHAR(50)            NOT NULL,
    data_ingresso DATE                   NOT NULL,
    data_uscita   DATE,
    stato         enrollment_status_enum NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_enrollment UNIQUE (person_id, group_id, season_id)
);

CREATE TABLE person_cache
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    person_id      UUID        NOT NULL UNIQUE,
    nome           VARCHAR(100),
    cognome        VARCHAR(100),
    codice_fiscale VARCHAR(16),
    email          VARCHAR(255),
    last_synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source         VARCHAR(50) NOT NULL DEFAULT 'sync_check'
);

CREATE INDEX idx_membership_lookup ON membership (person_id, asd_id, season_id, stato);
CREATE INDEX idx_enrollment_group ON group_enrollment (group_id, season_id, stato);

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW(); RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_membership_updated_at
    BEFORE UPDATE
    ON membership
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_role_assignment_updated_at
    BEFORE UPDATE
    ON role_assignment
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_groups_updated_at
    BEFORE UPDATE
    ON groups
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_group_enrollment_updated_at
    BEFORE UPDATE
    ON group_enrollment
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
