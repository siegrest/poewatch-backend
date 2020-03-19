CREATE TABLE IF NOT EXISTS statistic_history
(
    time  TIMESTAMP   NOT NULL,
    type  VARCHAR(64) NOT NULL,
    value BIGINT      NOT NULL
);

ALTER TABLE statistic_history
    ADD CONSTRAINT pk_statistic_history
        PRIMARY KEY (time, type)
;
