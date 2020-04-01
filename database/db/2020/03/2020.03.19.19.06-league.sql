CREATE TABLE IF NOT EXISTS league
(
    id         SERIAL      NOT NULL,
    active     BOOLEAN     NOT NULL,
    challenge  BOOLEAN     NOT NULL,
    display    VARCHAR(64),
    end_time   TIMESTAMP,
    event      BOOLEAN     NOT NULL,
    hardcore   BOOLEAN     NOT NULL,
    name       VARCHAR(64) NOT NULL,
    start_time TIMESTAMP,
    upcoming   BOOLEAN     NOT NULL
);

ALTER TABLE league
    ADD CONSTRAINT pk_league
        PRIMARY KEY (id)
;

ALTER TABLE league
    ADD CONSTRAINT uq_league_name
        UNIQUE (name)
;
