package org.app.common.redis;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.io.*;

public class RedisCompressor {

    private static final LZ4Factory factory = LZ4Factory.fastestInstance();
    private static final LZ4Compressor compressor = factory.fastCompressor();
    private static final LZ4FastDecompressor decompressor = factory.fastDecompressor();

    // Serialize object -> byte[]
    private static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        }
    }

    // Deserialize byte[] -> object
    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }

    // Compress object
    public static <T> byte[] compress(T obj) {
        try {
            byte[] input = serialize(obj);
            int maxLen = compressor.maxCompressedLength(input.length);
            byte[] compressed = new byte[maxLen];
            int compressedLen = compressor.compress(input, 0, input.length, compressed, 0, maxLen);
            byte[] output = new byte[compressedLen];
            System.arraycopy(compressed, 0, output, 0, compressedLen);
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Compression failed", e);
        }
    }

    // Decompress to object
    public static <T> T decompress(byte[] compressed, int originalLength, Class<T> clazz) {
        try {
            byte[] restored = new byte[originalLength];
            decompressor.decompress(compressed, 0, restored, 0, originalLength);
            return clazz.cast(deserialize(restored));
        } catch (Exception e) {
            throw new RuntimeException("Decompression failed", e);
        }
    }
}
