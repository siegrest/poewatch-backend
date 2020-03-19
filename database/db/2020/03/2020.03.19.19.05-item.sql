CREATE TABLE IF NOT EXISTS item
(
    id                 INTEGER      NOT NULL,
    corrupted          BOOLEAN,
    enchantment_max    DOUBLE PRECISION,
    enchantment_min    DOUBLE PRECISION,
    found              TIMESTAMP    NOT NULL,
    gem_level          INTEGER,
    gem_quality        INTEGER,
    icon               VARCHAR(255) NOT NULL,
    influence_crusader BOOLEAN,
    influence_elder    BOOLEAN,
    influence_hunter   BOOLEAN,
    influence_redeemer BOOLEAN,
    influence_shaper   BOOLEAN,
    influence_warlord  BOOLEAN,
    item_level         INTEGER,
    links              INTEGER,
    map_series         INTEGER,
    map_tier           INTEGER,
    stack_size         INTEGER,
    variation          VARCHAR(32),
    item_base_id       INTEGER      NOT NULL
);


ALTER TABLE item
    ADD CONSTRAINT pk_item
        PRIMARY KEY (id)
;

ALTER TABLE item
    ADD CONSTRAINT uq_item
        UNIQUE (item_base_id, stack_size, item_level, links, corrupted, variation, map_tier, map_series,
                influence_shaper,
                influence_elder, influence_crusader, influence_redeemer, influence_hunter, influence_warlord,
                enchantment_min, enchantment_max, gem_level, gem_quality)
;

ALTER TABLE item
    ADD CONSTRAINT fk_item_base
        FOREIGN KEY (item_base_id) REFERENCES pw.item_base (id) ON DELETE CASCADE
;
