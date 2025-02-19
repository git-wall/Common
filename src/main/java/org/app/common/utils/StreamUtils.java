package org.app.common.utils;

import java.io.*;
import java.util.Collection;
import java.util.stream.Stream;

public class StreamUtils {
    public static final int STREAM_MAX_SIZE = 1000;

    public static <T> Stream<T> of(Collection<T> collection) {
        return collection.size() <= STREAM_MAX_SIZE ? collection.stream() : collection.parallelStream();
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public static Object deserialize(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}
