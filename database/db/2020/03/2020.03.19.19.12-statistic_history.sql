CREATE TABLE IF NOT EXISTS statistic_history
(
    type  VARCHAR(64) NOT NULL,
    value BIGINT      NOT NULL,
    time  TIMESTAMP   NOT NULL
);

ALTER TABLE statistic_history
    ADD CONSTRAINT pk_statistic_history
        PRIMARY KEY (time, type)
;
