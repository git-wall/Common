package org.app.common.line;

import lombok.extern.slf4j.Slf4j;
import org.app.common.utils.NetworkUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
public class LineNotify {

    private final static String URL_LINE = "https://notify-api.line.me/api/notify";

    private final static URI URI_LINE;

    static {
        URI_LINE = URI.create(URL_LINE);
    }

    public static boolean notifyLine(String token, String topic, String message) {
        try {
            HttpClient client = getHttpClient();
            HttpRequest request = buildHttpRequest(token, getMessageLine(topic, message));

            HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            return res.statusCode() == 200;
        } catch (Exception ex) {
            log.error("Error notify line", ex);
        }
        return false;
    }

    public static boolean notifyLineAsync(String auth, String topic, String message) {
        try {
            HttpClient client = getHttpClient();
            HttpRequest request = buildHttpRequest(auth, getMessageLine(topic, message));

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .thenApply(h -> h.statusCode() == 200)
                    .get();
        } catch (Exception ex) {
            log.error("Error notify line", ex);
        }
        return false;
    }

    private static HttpRequest buildHttpRequest(String auth, String messageLine) {
        return HttpRequest.newBuilder()
                .uri(URI_LINE)
                .timeout(Duration.ofSeconds(2L))
                .headers(
                        "Content-Type", "application/x-www-form-urlencoded",
                        "Authorization", String.format("Bearer %s", auth)
                )
                .POST(HttpRequest.BodyPublishers.ofString(messageLine))
                .build();
    }

    private static HttpClient getHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10L))
                //				.authenticator(Authenticator.getDefault())
                .build();
    }

    private static String getMessageLine(String topic, String message) {
        String mess = String.join("\n", topic, message, NetworkUtils.getLocalHostName());
        return String.format("message = %s", URLEncoder.encode(mess, StandardCharsets.UTF_8));
    }

}
