CREATE TABLE IF NOT EXISTS category
(
    id      INTEGER     NOT NULL,
    display VARCHAR(32) NOT NULL,
    name    VARCHAR(32) NOT NULL
);

ALTER TABLE category
    ADD CONSTRAINT pk_category
        PRIMARY KEY (id)
;

ALTER TABLE category
    ADD CONSTRAINT uq_category_name
        UNIQUE (name)
;
