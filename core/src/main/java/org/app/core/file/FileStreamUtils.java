package org.app.core.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileStreamUtils {

    public static void copy(InputStream in, OutputStream out) {
        byte[] buffer = new byte[8192];
        int len;
        try (in; out) {
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long copyLarge(InputStream in, OutputStream out) {
        byte[] buffer = new byte[8192];
        long total = 0;
        int len;
        try (in; out) {
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                total += len;
            }
            return total;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
