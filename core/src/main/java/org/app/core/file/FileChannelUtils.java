package org.app.core.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileChannelUtils {

    public static void transfer(Path path, WritableByteChannel target) {
        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
            fc.transferTo(0, fc.size(), target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer readToBuffer(Path path) {
        try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) fc.size());
            fc.read(buffer);
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
