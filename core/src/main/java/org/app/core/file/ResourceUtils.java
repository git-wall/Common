package org.app.core.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUtils {

    public static InputStream getResourceAsStream(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(name);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + name);
        }
        return is;
    }

    public static String readResource(String name) {
        try (
            InputStream is = getResourceAsStream(name);
            BufferedReader br = new BufferedReader(new InputStreamReader(is))
        ) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
