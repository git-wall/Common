package org.app.common.utils;

import lombok.SneakyThrows;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileUtils {

    private FileUtils() {
    }

    @SneakyThrows
    public static String pathFromResource(String filename) {
        return Optional.ofNullable(FileUtils.class.getResource(filename))
                .map(URL::toString)
                .orElseThrow(() -> new FileNotFoundException("File not found with name :" + filename));
    }

    public static File fileFromResource(String filename) throws FileNotFoundException {
        return Optional.ofNullable(FileUtils.class.getResource(filename))
                .map(url -> new File(url.getFile()))
                .orElseThrow(() -> new FileNotFoundException("File not found with name :" + filename));
    }

    @NonNull
    public static File fileFromPath(String filename) {
        return new File(filename);
    }

    @Nullable
    @SneakyThrows
    public static String readFromResource(String filename) {
        InputStream is = FileUtils.class.getResourceAsStream(filename);
        return Optional.ofNullable(is)
                .map(FileUtils::readFromInputStream)
                .orElse(null);
    }

    @NonNull
    @SneakyThrows
    public static String readFromInputStream(InputStream inputStream) {
        return bufferReadFile(inputStream);
    }

    @NonNull
    @SneakyThrows
    public static String readFromPath(String path) {
        FileInputStream fis = new FileInputStream(path);
        return bufferReadFile(fis);
    }

    @NonNull
    @SneakyThrows
    public static String readFromFile(File file) {
        FileInputStream fis = new FileInputStream(file);
        return bufferReadFile(fis);
    }

    @NonNull
    @SneakyThrows
    private static String bufferReadFile(InputStream inputStream) {
        Function<BufferedReader, String> readerFunction = br -> br.lines()
                .map(line -> line + "\n")
                .collect(Collectors.joining());
        return bufferReadFile(readerFunction, inputStream);
    }

    @SneakyThrows
    public static <T> T bufferReadFile(Function<BufferedReader, T> readerFunction, InputStream inputStream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return readerFunction.apply(br);
        }
    }
}

