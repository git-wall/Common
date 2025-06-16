package org.app.common.video;

import org.springframework.core.io.AbstractResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

class PartialFileResource extends AbstractResource {
    private final File file;
    private final long start;
    private final long contentLength;

    public PartialFileResource(File file, long start, long contentLength) {
        this.file = file;
        this.start = start;
        this.contentLength = contentLength;
    }

    @Override
    public String getDescription() {
        return "Partial file [" + file.getAbsolutePath() + "]";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        randomAccessFile.seek(start);
        return new PartialInputStream(randomAccessFile, contentLength);
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }
}