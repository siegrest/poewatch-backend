CREATE TABLE IF NOT EXISTS league
(
    id         SERIAL      NOT NULL,
    name       VARCHAR(64) NOT NULL,
    display    VARCHAR(64),
    active     BOOLEAN     NOT NULL,
    challenge  BOOLEAN     NOT NULL,
    event      BOOLEAN     NOT NULL,
    hardcore   BOOLEAN     NOT NULL,
    upcoming   BOOLEAN     NOT NULL,
    end_time   TIMESTAMP,
    start_time TIMESTAMP
);

ALTER TABLE league
    ADD CONSTRAINT pk_league
        PRIMARY KEY (id)
;

ALTER TABLE league
    ADD CONSTRAINT uq_league_name
        UNIQUE (name)
;
