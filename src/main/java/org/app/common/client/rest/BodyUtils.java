package org.app.common.client.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.app.common.utils.JacksonUtils;

public class BodyUtils {

    public static String getBody(final Object input) {
        ObjectNode requestBody = JacksonUtils.createObjectNode();
        requestBody.set("input", JacksonUtils.valueToTree(input));
        return JacksonUtils.writeValueAsString(requestBody);
    }
}
