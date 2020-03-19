CREATE TABLE IF NOT EXISTS league_item_entry
(
    id            BIGINT    NOT NULL,
    found         TIMESTAMP NOT NULL,
    price         DOUBLE PRECISION,
    seen          TIMESTAMP NOT NULL,
    stack_size    INTEGER,
    updates       INTEGER   NOT NULL,
    item_id       INTEGER   NOT NULL,
    price_item_id INTEGER,
    stash_id      BIGINT
);

ALTER TABLE league_item_entry
    ADD CONSTRAINT pk_league_item_entry
        PRIMARY KEY (id)
;

ALTER TABLE league_item_entry
    ADD CONSTRAINT fk_item
        FOREIGN KEY (item_id) REFERENCES pw.item (id) ON DELETE CASCADE
;

ALTER TABLE league_item_entry
    ADD CONSTRAINT fk_price_item
        FOREIGN KEY (item_id) REFERENCES pw.item (id) ON DELETE CASCADE
;

ALTER TABLE league_item_entry
    ADD CONSTRAINT fk_stash
        FOREIGN KEY (stash_id) REFERENCES pw.stash (id) ON DELETE CASCADE
;
