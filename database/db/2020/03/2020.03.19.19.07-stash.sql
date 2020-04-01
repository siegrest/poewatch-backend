CREATE TABLE IF NOT EXISTS stash
(
    id         BIGSERIAL NOT NULL,
    found      TIMESTAMP NOT NULL,
    item_count INTEGER   NOT NULL,
    seen       TIMESTAMP NOT NULL,
    stale      BOOLEAN,
    updates    INTEGER   NOT NULL,
    account_id BIGINT,
    league_id  INTEGER
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
