CREATE TABLE IF NOT EXISTS league_item_entry
(
    id            BIGSERIAL NOT NULL,
    item_id       INTEGER   NOT NULL,
    stash_id      BIGINT,
    stack_size    INTEGER,
    price         DOUBLE PRECISION,
    price_item_id INTEGER,
    updates       INTEGER   NOT NULL DEFAULT 0,
    found         TIMESTAMP DEFAULT now(),
    seen          TIMESTAMP DEFAULT now()
);

ALTER TABLE league_item_entry
    ADD CONSTRAINT pk_league_item_entry
        PRIMARY KEY (id)
;

ALTER TABLE league_item_entry
    ADD CONSTRAINT fk_item
        FOREIGN KEY (item_id) REFERENCES pw.item_detail (id) ON DELETE CASCADE
;

ALTER TABLE league_item_entry
    ADD CONSTRAINT fk_price_item
        FOREIGN KEY (item_id) REFERENCES pw.item_detail (id) ON DELETE CASCADE
;

ALTER TABLE league_item_entry
    ADD CONSTRAINT fk_stash
        FOREIGN KEY (stash_id) REFERENCES pw.stash (id) ON DELETE CASCADE
;
