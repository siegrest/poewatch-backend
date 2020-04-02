CREATE TABLE IF NOT EXISTS stash
(
    id         BIGSERIAL NOT NULL,
    league_id  INTEGER,
    account_id BIGINT,
    updates    INTEGER   NOT NULL,
    stale      BOOLEAN,
    found      TIMESTAMP NOT NULL,
    seen       TIMESTAMP NOT NULL
);

ALTER TABLE stash
    ADD CONSTRAINT pk_stash
        PRIMARY KEY (id)
;

ALTER TABLE stash
    ADD CONSTRAINT fk_league
        FOREIGN KEY (league_id) REFERENCES pw.league (id) ON DELETE CASCADE
;
ALTER TABLE stash
    ADD CONSTRAINT fk_account
        FOREIGN KEY (account_id) REFERENCES pw.account (id) ON DELETE CASCADE
;
