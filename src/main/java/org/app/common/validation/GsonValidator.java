package org.app.common.validation;

import com.google.gson.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GsonValidator {

    private static final TypeAdapter<JsonElement> strictAdapter;

    static {
        strictAdapter = new Gson().getAdapter(JsonElement.class);
    }

    public static boolean isValid(String json) {
        try {
            JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            return false;
        }
        return true;
    }

    public static boolean isValidStrict(String json) {
        try {
            strictAdapter.fromJson(json);
        } catch (JsonSyntaxException | IOException e) {
            return false;
        }
        return true;
    }
}
