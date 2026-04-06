package org.app.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.ByteBuffer;

/**
 * LZ4 compression utilities.
 * <p>
 * Wire format: [4 bytes original length BE] + [LZ4 compressed bytes]
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Compression {

    private static final LZ4Factory FACTORY = LZ4Factory.fastestInstance();

    public static byte[] compress(byte[] data) {
        if (data == null || data.length == 0) return data;
        LZ4Compressor compressor = FACTORY.fastCompressor();
        int maxLen = compressor.maxCompressedLength(data.length);
        byte[] buf = new byte[4 + maxLen];
        ByteBuffer.wrap(buf, 0, 4).putInt(data.length);
        int compressedSize = compressor.compress(data, 0, data.length, buf, 4, maxLen);
        byte[] out = new byte[4 + compressedSize];
        System.arraycopy(buf, 0, out, 0, out.length);
        return out;
    }

    public static byte[] decompress(byte[] compressedWithHeader) {
        if (compressedWithHeader == null || compressedWithHeader.length == 0) return compressedWithHeader;
        int originalLength = ByteBuffer.wrap(compressedWithHeader, 0, 4).getInt();
        LZ4FastDecompressor decompressor = FACTORY.fastDecompressor();
        byte[] restored = new byte[originalLength];
        decompressor.decompress(compressedWithHeader, 4, restored, 0, originalLength);
        return restored;
    }
}
