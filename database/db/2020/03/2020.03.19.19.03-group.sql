CREATE TABLE IF NOT EXISTS "group"
(
    id      SERIAL      NOT NULL,
    name    VARCHAR(32) NOT NULL,
    display VARCHAR(32)
);

ALTER TABLE "group"
    ADD CONSTRAINT pk_group
        PRIMARY KEY (id)
;

ALTER TABLE "group"
    ADD CONSTRAINT uq_group_name
        UNIQUE (name)
;
