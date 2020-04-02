CREATE TABLE IF NOT EXISTS league_item
(
    id        BIGSERIAL        NOT NULL,
    league_id INTEGER          NOT NULL,
    item_id   INTEGER          NOT NULL,
    current   INTEGER          NOT NULL,
    accepted  INTEGER          NOT NULL,
    daily     INTEGER          NOT NULL,
    total     INTEGER          NOT NULL,
    mean      DOUBLE PRECISION NOT NULL,
    median    DOUBLE PRECISION NOT NULL,
    mode      DOUBLE PRECISION NOT NULL,
    min       DOUBLE PRECISION NOT NULL,
    max       DOUBLE PRECISION NOT NULL,
    found     TIMESTAMP        NOT NULL,
    seen      TIMESTAMP        NOT NULL
);

ALTER TABLE league_item
    ADD CONSTRAINT pk_league_item
        PRIMARY KEY (id)
;

ALTER TABLE league_item
    ADD CONSTRAINT uq_league_item
        UNIQUE (league_id, item_id)
;

ALTER TABLE league_item
    ADD CONSTRAINT fk_item
        FOREIGN KEY (item_id) REFERENCES pw.item (id) ON DELETE CASCADE
;

ALTER TABLE league_item
    ADD CONSTRAINT fk_league
        FOREIGN KEY (league_id) REFERENCES pw.league (id) ON DELETE CASCADE
;
