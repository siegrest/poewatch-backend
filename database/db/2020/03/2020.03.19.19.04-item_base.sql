CREATE TABLE IF NOT EXISTS item_base
(
    id          SERIAL       NOT NULL,
    category_id INTEGER      NOT NULL,
    group_id    INTEGER      NOT NULL,
    frame_type  VARCHAR(255) NOT NULL,
    base_type   VARCHAR(64),
    name        VARCHAR(128),
    found       TIMESTAMP
);

ALTER TABLE item_base
    ADD CONSTRAINT pk_item_base
        PRIMARY KEY (id)
;

ALTER TABLE item_base
    ADD CONSTRAINT uq_item_base
        UNIQUE (name, category_id, group_id, base_type, frame_type)
;

ALTER TABLE item_base
    ADD CONSTRAINT fk_category
        FOREIGN KEY (category_id) REFERENCES pw.category (id) ON DELETE CASCADE
;

ALTER TABLE item_base
    ADD CONSTRAINT fk_group
        FOREIGN KEY (group_id) REFERENCES pw.group (id) ON DELETE CASCADE
;
