package org.app.common.module.video;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

class PartialInputStream extends InputStream {
    private final RandomAccessFile randomAccessFile;
    private long remainingBytes;

    public PartialInputStream(RandomAccessFile randomAccessFile, long contentLength) {
        this.randomAccessFile = randomAccessFile;
        this.remainingBytes = contentLength;
    }

    @Override
    public int read() throws IOException {
        if (remainingBytes <= 0) {
            return -1;
        }
        remainingBytes--;
        return randomAccessFile.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (remainingBytes <= 0) {
            return -1;
        }

        int toRead = (int) Math.min(len, remainingBytes);
        int bytesRead = randomAccessFile.read(b, off, toRead);

        if (bytesRead > 0) {
            remainingBytes -= bytesRead;
        }

        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }
}
