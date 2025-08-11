package org.app.common.validation;

import com.google.gson.*;

import java.io.IOException;

public class GsonValidator {

    private static final TypeAdapter<JsonElement> strictAdapter = new Gson().getAdapter(JsonElement.class);

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
