package org.app.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.app.common.utils.JacksonUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Slf4j
public class Telegram {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";

    /**
     * Sends a synchronous notification to a Telegram channel.
     *
     * @param botToken The bot token for authentication.
     * @param chatId   The ID of the chat/channel where the message will be sent.
     * @param topic    The topic of the message.
     * @param message  The main content of the message.
     * @return True if the message was sent successfully, false otherwise.
     */
    public static boolean notifyTelegram(String botToken, String chatId, String topic, String message) {
        try {
            HttpClient client = getHttpClient();
            HttpRequest request = buildHttpRequest(botToken, chatId, getMessageTelegram(topic, message));
            HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return res.statusCode() == 200 && parseTelegramResponse(res.body());
        } catch (Exception ex) {
            log.error("Error notifying Telegram", ex);
        }
        return false;
    }

    /**
     * Sends an asynchronous notification to a Telegram channel.
     *
     * @param botToken The bot token for authentication.
     * @param chatId   The ID of the chat/channel where the message will be sent.
     * @param topic    The topic of the message.
     * @param message  The main content of the message.
     * @return True if the message was sent successfully, false otherwise.
     */
    public static boolean notifyTelegramAsync(String botToken, String chatId, String topic, String message) {
        try {
            HttpClient client = getHttpClient();
            HttpRequest request = buildHttpRequest(botToken, chatId, getMessageTelegram(topic, message));
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .thenApply(res -> res.statusCode() == 200 && parseTelegramResponse(res.body()))
                    .get();
        } catch (Exception ex) {
            log.error("Error notifying Telegram asynchronously", ex);
        }
        return false;
    }

    /**
     * Builds the HTTP request for sending a message to Telegram.
     *
     * @param botToken The bot token for authentication.
     * @param chatId   The ID of the chat/channel where the message will be sent.
     * @param message  The message payload.
     * @return The constructed HTTP request.
     */
    private static HttpRequest buildHttpRequest(String botToken, String chatId, String message) {
        String url = String.format(TELEGRAM_API_URL, botToken);
        String jsonPayload = String.format("{\"chat_id\": \"%s\", \"text\": \"%s\"}", chatId, message);

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5L))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
    }

    /**
     * Creates and configures the HTTP client.
     *
     * @return A configured HTTP client.
     */
    private static HttpClient getHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10L))
                .build();
    }

    /**
     * Constructs the message to be sent to Telegram.
     *
     * @param topic   The topic of the message.
     * @param message The main content of the message.
     * @return The formatted message string.
     */
    private static String getMessageTelegram(String topic, String message) {
        return String.join("\n", topic, message); // No need to encode for Telegram
    }

    /**
     * Parses the Telegram API response to determine success.
     *
     * @param responseBody The response body from the Telegram API.
     * @return True if the response indicates success, false otherwise.
     */
    private static boolean parseTelegramResponse(String responseBody) {
        try {
            Map<String, Object> responseMap = JacksonUtils.readValue(responseBody);
            Boolean ok = (Boolean) responseMap.get("ok");
            return Boolean.TRUE.equals(ok);
        } catch (Exception ex) {
            log.error("Error parsing Telegram response", ex);
            return false;
        }
    }
}
