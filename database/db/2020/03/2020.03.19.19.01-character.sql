CREATE TABLE IF NOT EXISTS character
(
    id         BIGSERIAL   NOT NULL,
    name       VARCHAR(32) NOT NULL,
    account_id BIGINT      NOT NULL,
    found      TIMESTAMP DEFAULT now(),
    seen       TIMESTAMP DEFAULT now()
);

ALTER TABLE character
    ADD CONSTRAINT pk_character
        PRIMARY KEY (id)
;

ALTER TABLE character
    ADD CONSTRAINT uq_character_name
        UNIQUE (name)
;

ALTER TABLE character
    ADD CONSTRAINT fk_account
        FOREIGN KEY (account_id) REFERENCES pw.account (id) ON DELETE CASCADE
;