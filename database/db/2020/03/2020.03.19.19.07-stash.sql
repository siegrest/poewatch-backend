CREATE TABLE IF NOT EXISTS stash
(
    id         BIGSERIAL NOT NULL,
    poe_id     CHAR(64)  NOT NULL,
    league_id  INTEGER,
    account_id BIGINT,
    updates    INTEGER   NOT NULL DEFAULT 1,
    stale      BOOLEAN   NOT NULL DEFAULT FALSE,
    found      TIMESTAMP          DEFAULT now(),
    seen       TIMESTAMP          DEFAULT now()
);

ALTER TABLE stash
    ADD CONSTRAINT pk_stash
        PRIMARY KEY (id)
;

ALTER TABLE stash
    ADD CONSTRAINT uq_stash
        UNIQUE (poe_id)
;

ALTER TABLE stash
    ADD CONSTRAINT fk_league
        FOREIGN KEY (league_id) REFERENCES pw.league (id) ON DELETE CASCADE
;
ALTER TABLE stash
    ADD CONSTRAINT fk_account
        FOREIGN KEY (account_id) REFERENCES pw.account (id) ON DELETE CASCADE
;
