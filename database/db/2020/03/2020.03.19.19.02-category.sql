CREATE TABLE IF NOT EXISTS category
(
    id      SERIAL      NOT NULL,
    name    VARCHAR(32) NOT NULL,
    display VARCHAR(32)
);

ALTER TABLE category
    ADD CONSTRAINT pk_category
        PRIMARY KEY (id)
;

ALTER TABLE category
    ADD CONSTRAINT uq_category_name
        UNIQUE (name)
;
