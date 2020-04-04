INSERT INTO pw.statistic_collector(type, group_type, timespan, start, count, sum)
VALUES ('TIME_API_REPLY_DOWNLOAD', 'AVG', 3600000, null, 0, 0),
       ('TIME_API_TTFB', 'AVG', 3600000, null, 0, 0),

       ('TIME_REPLY_PARSE', 'AVG', 3600000, null, 0, 0),
       ('TIME_REPLY_DESERIALIZE', 'AVG', 3600000, null, 0, 0),
       ('TIME_INDEX_ITEM', 'AVG', 3600000, null, 0, 0),
       ('TIME_PROCESS_RIVER', 'AVG', 3600000, null, 0, 0),
       ('TIME_PERSIST_STASHES', 'AVG', 3600000, null, 0, 0),
       ('TIME_MARK_ITEMS_STALE', 'AVG', 3600000, null, 0, 0),
       ('TIME_PERSIST_STASH_ENTRIES', 'AVG', 3600000, null, 0, 0),
       ('TIME_PERSIST_ACCOUNT', 'AVG', 3600000, null, 0, 0),
       ('TIME_PERSIST_CHARACTER', 'AVG', 3600000, null, 0, 0),
       ('TIME_WORKERS_IDLE', 'AVG', 3600000, null, 0, 0),

       ('COUNT_API_ERRORS_READ_TIMEOUT', 'AVG', 3600000, null, 0, 0),
       ('COUNT_API_ERRORS_CONNECT_TIMEOUT', 'AVG', 3600000, null, 0, 0),
       ('COUNT_API_ERRORS_CONN_RESET', 'AVG', 3600000, null, 0, 0),
       ('COUNT_API_ERRORS_5XX', 'AVG', 3600000, null, 0, 0),
       ('COUNT_API_ERRORS_4XX', 'AVG', 3600000, null, 0, 0),
       ('COUNT_API_ERRORS_DUPLICATE', 'SUM', 3600000, null, 0, 0),

       ('COUNT_REPLY_SIZE', 'AVG', 3600000, null, 0, 0),
       ('COUNT_API_CALLS', 'COUNT', 3600000, null, 0, 0),
       ('COUNT_TOTAL_STASHES', 'SUM', 3600000, null, 0, 0),
       ('COUNT_TOTAL_ITEMS', 'SUM', 3600000, null, 0, 0),
       ('COUNT_ACCEPTED_ITEMS', 'SUM', 3600000, null, 0, 0),
       ('COUNT_ACTIVE_ACCOUNTS', 'SUM', 3600000, null, 0, 0),

       ('MISC_APP_STARTUP', 'COUNT', 3600000, null, 0, 0),
       ('MISC_APP_SHUTDOWN', 'COUNT', 3600000, null, 0, 0);

