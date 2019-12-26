package watch.poe.app.utility;

import watch.poe.app.domain.StatType;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public final class HttpUtility {

    private static final Pattern exceptionPattern5xx = Pattern.compile("^.+ 5\\d\\d .+$");
    private static final Pattern exceptionPattern4xx = Pattern.compile("^.+ 4\\d\\d .+$");

    public static String fetch(String url) throws IOException {
        InputStream stream = null;

        try {
            URL request = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) request.openConnection();

            connection.setReadTimeout(5000);
            connection.setConnectTimeout(2500);

            stream = connection.getInputStream();
            var stringBuilder = new StringBuilder();
            var byteBuffer = new byte[256];
            int byteCount;

            while ((byteCount = stream.read(byteBuffer, 0, 256)) != -1) {
                if (byteCount != 256) {
                    byte[] trimmedByteBuffer = new byte[byteCount];
                    System.arraycopy(byteBuffer, 0, trimmedByteBuffer, 0, byteCount);
                    stringBuilder.append(new String(trimmedByteBuffer));
                } else {
                    stringBuilder.append(new String(byteBuffer));
                }
            }

            return stringBuilder.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static StatType getErrorType(Exception ex) {
        if (ex.getMessage().contains("Read timed out")) {
            return StatType.COUNT_API_ERRORS_READ_TIMEOUT;
        }

        if (ex.getMessage().contains("connect timed out")) {
            return StatType.COUNT_API_ERRORS_CONNECT_TIMEOUT;
        }

        if (ex.getMessage().contains("Connection reset")) {
            return StatType.COUNT_API_ERRORS_CONN_RESET;
        }

        if (exceptionPattern5xx.matcher(ex.getMessage()).matches()) {
            return StatType.COUNT_API_ERRORS_5XX;
        }

        if (exceptionPattern4xx.matcher(ex.getMessage()).matches()) {
            return StatType.COUNT_API_ERRORS_4XX;
        }

        return null;
    }

}
