CREATE TABLE IF NOT EXISTS statistic_partial
(
    type  VARCHAR(32) NOT NULL,
    time  TIMESTAMP   NOT NULL,
    count BIGINT      NOT NULL,
    sum   BIGINT      NOT NULL
);

ALTER TABLE statistic_partial
    ADD CONSTRAINT pk_statistic_partial
        PRIMARY KEY (type, time)
;
