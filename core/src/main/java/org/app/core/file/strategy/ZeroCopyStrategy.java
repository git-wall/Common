package org.app.core.file.strategy;

import org.app.core.file.FileChannelUtils;

import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;

public class ZeroCopyStrategy implements FileAccessStrategy {

    private final WritableByteChannel target;

    public ZeroCopyStrategy(WritableByteChannel target) {
        this.target = target;
    }

    @Override
    public void read(Path path) {
        FileChannelUtils.transfer(path, target);
    }
}
