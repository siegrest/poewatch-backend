package watch.poe.app.domain;

public enum StatType {
    TIME_API_REPLY_DOWNLOAD,
    TIME_API_TTFB,
    TIME_REPLY_PARSE,
    TIME_REPLY_DESERIALIZE,

    COUNT_API_ERRORS_READ_TIMEOUT,
    COUNT_API_ERRORS_CONNECT_TIMEOUT,
    COUNT_API_ERRORS_CONN_RESET,
    COUNT_API_ERRORS_5XX,
    COUNT_API_ERRORS_4XX,
    COUNT_API_ERRORS_DUPLICATE,

    COUNT_REPLY_SIZE,
    COUNT_API_CALLS,
    COUNT_TOTAL_STASHES,
    COUNT_TOTAL_ITEMS,
    COUNT_ACCEPTED_ITEMS,
    COUNT_ACTIVE_ACCOUNTS,

    MISC_APP_STARTUP,
    MISC_APP_SHUTDOWN,

}
