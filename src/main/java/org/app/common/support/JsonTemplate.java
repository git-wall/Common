package org.app.common.support;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.app.common.pattern.legacy.FluentApi;
import org.app.common.utils.JacksonUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

public class JsonTemplate extends FluentApi<JsonTemplate> {
    @Getter
    private String template;
    @Getter
    private JsonNode treeNode;

    private final Map<String, String> fields;
    private List<Object> objects;

    public JsonTemplate(String template) {
        super();
        this.template = template;
        this.fields = new HashMap<>(16, 0.75f);
    }

    public static JsonTemplate of(String template) {
        return new JsonTemplate(template);
    }

    public JsonTemplate addObject(Object... os) {
        if (objects == null) objects = new ArrayList<>();
        objects.addAll(List.of(os));
        return self();
    }

    public JsonTemplate addObject(Object object) {
        if (objects == null) objects = new ArrayList<>();
        objects.add(object);
        return self();
    }

    public JsonTemplate build() {
        Assert.isTrue(objects != null, "Objects is empty for process pipeline add field to template");
        objects.forEach(this::addFields);
        replaceValueTemplates();
        treeNode = JacksonUtils.readTree(template);
        JacksonUtils.replaceNullStrings(treeNode);
        return self();
    }

    @SneakyThrows
    private void addFields(Object o) {
        for (Field field : o.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            var fieldName = String.format("$%s", field.getName());

            if (fields.containsKey(fieldName)) continue;
            if (!template.contains(fieldName)) continue;

            var val = field.get(o);
            var newVal = Optional.ofNullable(val)
                    .map(Object::toString)
                    .orElse("null");

            fields.put(fieldName, newVal);

            field.setAccessible(false);
        }
    }

    private void replaceValueTemplates() {
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            template = template.replace(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Process the fun custom to get your result
     *
     * @return result of fun custom
     */
    public <T> T get(Function<JsonNode, T> function) {
        return function.apply(treeNode);
    }

    /**
     * Add a custom field value
     *
     * @param fieldName The field name (without $ prefix)
     * @param value     The value to set
     * @return This DataCollect instance for chaining
     */
    public JsonTemplate addCustomField(String fieldName, Object value) {
        String key = fieldName.startsWith("$") ? fieldName : "$" + fieldName;
        String stringValue = value != null ? value.toString() : "null";
        fields.put(key, stringValue);
        return self();
    }

    /**
     * Check if the template contains a specific field placeholder
     *
     * @param fieldName The field name (without $ prefix)
     * @return True if the template contains the field placeholder
     */
    public boolean containsField(String fieldName) {
        String key = fieldName.startsWith("$") ? fieldName : "$" + fieldName;
        return template.contains(key);
    }
}
