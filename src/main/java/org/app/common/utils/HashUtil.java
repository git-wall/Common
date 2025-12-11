package org.app.common.utils;

import java.nio.charset.StandardCharsets;

public class HashUtil {
    // Constants for the FNV1-a hashCode algorithm.
    public static final int FNV_HASH_INIT = 0x811c9dc5;
    public static final int FNV_HASH_PRIME = 0x01000193;

    // This class should never be instantiated - it contains only static utility methods.
    private HashUtil() {}

    /**
     * Convince method for computing the FNV-1a hash on the UTF-8 encoded byte representation of the given String.
     */
    public static int hashFnv1a32_UTF8(String string) {
        return hash(string, FNV_HASH_INIT);
    }


    /**
     * Convince method for computing the FNV-1a hash on the UTF-8 encoded byte representation of the given String,
     * using the given initial hash value.
     */
    public static int hash(String string, int init) {
        return hash(string.getBytes(StandardCharsets.UTF_8), init);
    }


    /**
     * FNV-1a hashing function.
     * <a href="https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function">Hash func </a>
     */
    public static int hashFnv1a32(byte[] data) {
        return hash(data, FNV_HASH_INIT);
    }


    /**
     * Computes a hash of the given bytes, using the FNV-1a hashing function, but with a custom initial hash value.
     */
    public static int hash(byte[] data, int init) {
        int hash = init;
        for (byte b : data) {
            hash = hash ^ (int) b;
            hash *= FNV_HASH_PRIME;
        }

        return hash;
    }
}
