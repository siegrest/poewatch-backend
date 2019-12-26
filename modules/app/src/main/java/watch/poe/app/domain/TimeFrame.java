package watch.poe.app.domain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TimeFrame {
    M_1, M_10, M_30, M_60, H_6, H_12, H_24;

    /**
     * Get length in milliseconds
     */
    public long asMilli() {
        switch (this) {
            case M_1:
                return 60000;
            case M_10:
                return 600000;
            case M_30:
                return 1800000;
            case M_60:
                return 3600000;
            case H_6:
                return 21600000;
            case H_12:
                return 43200000;
            case H_24:
                return 86400000;
            default:
                log.error("Attempted to access non-existent TimeFrame");
                throw new RuntimeException();
        }
    }

    /**
     * Gets milliseconds from now until the TimeFrame
     */
    public long getRemaining() {
        long current = System.currentTimeMillis();
        return asMilli() - (current - (current / asMilli()) * asMilli());
    }

    /**
     * Gets milliseconds from the TimeFrame until now
     */
    public long getElapsed() {
        long current = System.currentTimeMillis();
        return current - (current / asMilli()) * asMilli();
    }

    /**
     * Gets milliseconds from the start until the TimeFrame
     */
    public long getCurrent() {
        return (System.currentTimeMillis() / asMilli()) * asMilli();
    }

    /**
     * Gets milliseconds from the start until the next TimeFrame
     */
    public long getNext() {
        return (System.currentTimeMillis() / asMilli() + 1) * asMilli();
    }
}
