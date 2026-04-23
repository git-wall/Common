package org.app.core.file.strategy;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MappedStrategy implements FileAccessStrategy {

    @Override
    public void read(Path path) {
        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
            MappedByteBuffer buffer = fc.map(
                FileChannel.MapMode.READ_ONLY,
                0,
                fc.size()
            );

            while (buffer.hasRemaining()) {
                System.out.print((char) buffer.get());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
