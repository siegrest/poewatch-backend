package watch.poe.app.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpUtility {

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

}
