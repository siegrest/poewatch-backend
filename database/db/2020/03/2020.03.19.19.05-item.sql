CREATE TABLE IF NOT EXISTS item
(
    id                 SERIAL       NOT NULL,
    item_base_id       INTEGER      NOT NULL,
    item_level         INTEGER,
    links              INTEGER,
    stack_size         INTEGER,
    variation          VARCHAR(32),
    corrupted          BOOLEAN,
    enchantment_max    DOUBLE PRECISION,
    enchantment_min    DOUBLE PRECISION,
    gem_level          INTEGER,
    gem_quality        INTEGER,
    influence_crusader BOOLEAN,
    influence_elder    BOOLEAN,
    influence_hunter   BOOLEAN,
    influence_redeemer BOOLEAN,
    influence_shaper   BOOLEAN,
    influence_warlord  BOOLEAN,
    map_series         INTEGER,
    map_tier           INTEGER,
    icon               VARCHAR(255) NOT NULL,
    found              TIMESTAMP    NOT NULL,
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
