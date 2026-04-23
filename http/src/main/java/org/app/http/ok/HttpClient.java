package org.app.http.ok;

import okhttp3.*;
import org.app.jackson.JacksonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class HttpClient {
    private static final OkHttpClient okHttpClient = new OkHttpClient();

    public static <T> T post(String url, Object data, String credential, Class<T> clazz) throws IOException {
        String response = post(url, data, credential);
        if (response != null) {
            return JacksonUtils.readValue(response, clazz);
        }
        return null;
    }

    public static String post(String url, Object data, String credential) throws IOException {
        MediaType MEDIA_TYPE = MediaType.parse("text/plain;charset=UTF-8");
        var body = JacksonUtils.writeValueAsBytes(data);
        RequestBody requestBody = RequestBody.create(body, MEDIA_TYPE);

        Request request = new Request.Builder().url(url)
            .post(requestBody)
            .header("Authorization", credential)
            .build();

        return call(request);
    }

    public static String postFile(String url, File file, String credential) throws IOException {
        var body = RequestBody.create(file, MediaType.parse("multipart/form-data"));
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.getName(), body)
            .build();
        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Authorization", credential)
            .build();

        return call(request, res -> "Unexpected code " + res);
    }

    public static String postForm(String url, Map<String, String> fromData, String credential) throws IOException {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        fromData.forEach(formBodyBuilder::addEncoded);
        RequestBody formBody = formBodyBuilder.build();

        Request request = new Request.Builder()
            .url(url)
            .post(formBody)
            .header("Authorization", credential)
            .build();

        return Objects.requireNonNull(call(request, res -> "Unexpected code " + res));
    }

    public static String call(Request request, Function<Response, String> message) throws IOException {
        try (var response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            }

            throw new IOException(message.apply(response));
        }
    }

    public static String call(Request request) throws IOException {
        try (var response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            }

            return null;
        }
    }
}
