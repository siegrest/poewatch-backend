CREATE TABLE IF NOT EXISTS change_id
(
    id        VARCHAR(255) NOT NULL,
    change_id VARCHAR(64),
    updated   TIMESTAMP
);

ALTER TABLE change_id
    ADD CONSTRAINT pk_change_id
        PRIMARY KEY (id)
;
