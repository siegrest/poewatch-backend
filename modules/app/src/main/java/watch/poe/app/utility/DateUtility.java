package watch.poe.app.utility;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
public final class DateUtility {

    public static SimpleDateFormat isoSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        isoSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Date parseIso(String date) {
        if (date == null) {
            return null;
        }

        try {
            return isoSdf.parse(date);
        } catch (ParseException ex) {
            log.error("Could no parse iso date", ex);
            return null;
        }
    }

}
