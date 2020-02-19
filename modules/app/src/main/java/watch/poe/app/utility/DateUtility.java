package watch.poe.app.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtility {

    public static SimpleDateFormat isoSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

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
            return null;
        }
    }

}
