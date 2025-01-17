CREATE TABLE IF NOT EXISTS account
(
    id    BIGSERIAL   NOT NULL,
    name  VARCHAR(32) NOT NULL,
    found TIMESTAMP DEFAULT now(),
    seen  TIMESTAMP DEFAULT now()
);

ALTER TABLE account
    ADD CONSTRAINT pk_account
        PRIMARY KEY (id)
;

ALTER TABLE account
    ADD CONSTRAINT uq_account_name
        UNIQUE (name)
;