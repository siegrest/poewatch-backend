CREATE TABLE IF NOT EXISTS change_id
(
    type    VARCHAR(255) NOT NULL,
    value   VARCHAR(64),
    updated TIMESTAMP
);

ALTER TABLE change_id
    ADD CONSTRAINT pk_change_id
        PRIMARY KEY (type)
;
