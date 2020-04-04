CREATE TABLE IF NOT EXISTS statistic_collector
(
    type        VARCHAR(64) NOT NULL,
    group_type  VARCHAR(6)  NOT NULL,
    timespan    BIGINT      NOT NULL,
    start       TIMESTAMP,
    count       BIGINT      NOT NULL,
    sum         BIGINT      NOT NULL,
    description VARCHAR
);

ALTER TABLE statistic_collector
    ADD CONSTRAINT pk_statistic_collector
        PRIMARY KEY (type)
;
