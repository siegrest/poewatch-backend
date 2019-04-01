package poe.Managers.Stat;

public enum StatType {
    TIME_PARSE_REPLY,

    TIME_API_REPLY_DOWNLOAD,
    TIME_API_TTFB,
    COUNT_REPLY_SIZE,
    COUNT_API_CALLS,

    COUNT_API_ERRORS_READ_TIMEOUT,
    COUNT_API_ERRORS_CONNECT_TIMEOUT,
    COUNT_API_ERRORS_CONN_RESET,
    COUNT_API_ERRORS_5XX,
    COUNT_API_ERRORS_429,

    COUNT_TOTAL_STASHES,
    COUNT_TOTAL_ITEMS,
    COUNT_ACCEPTED_ITEMS,
    COUNT_ACTIVE_ACCOUNTS,

    COUNT_API_ERRORS_DUPLICATE,

    APP_STARTUP,
    APP_SHUTDOWN
}
