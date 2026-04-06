package org.app.core.support;

import lombok.NoArgsConstructor;

import java.io.*;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Convert {
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
