CREATE TABLE IF NOT EXISTS statistic_history
(
    id    BIGSERIAL   NOT NULL,
    type  VARCHAR(64) NOT NULL,
    time  TIMESTAMP   NOT NULL,
    value BIGINT      NOT NULL
);

ALTER TABLE statistic_history
    ADD CONSTRAINT pk_statistic_history
        PRIMARY KEY (id)
;
