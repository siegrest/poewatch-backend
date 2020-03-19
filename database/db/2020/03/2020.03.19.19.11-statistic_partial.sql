CREATE TABLE IF NOT EXISTS statistic_partial
(
    time  TIMESTAMP   NOT NULL,
    type  VARCHAR(32) NOT NULL,
    count BIGINT      NOT NULL,
    sum   BIGINT      NOT NULL
);

ALTER TABLE statistic_partial
    ADD CONSTRAINT pk_statistic_partial
        PRIMARY KEY (time, type)
;
