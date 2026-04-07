package org.app.common.optimize;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

// Memory-Mapped Files
public class LargeFileReader {

    public class Record {
        // Giả sử mỗi record có một trường dữ liệu kiểu String
        private String data;

        public Record(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    public List<Record> readLargeFile(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

            MappedByteBuffer buffer = channel.map(
                FileChannel.MapMode.READ_ONLY,
                0,
                channel.size()
            );

            List<Record> records = new ArrayList<>();

            // Đọc rất nhanh, OS tự động paging
            while (buffer.hasRemaining()) {
                Record record = readRecord(buffer);
                records.add(record);
            }

            return records;
        }
    }

    private Record readRecord(MappedByteBuffer buffer) {
        return null; // Thực hiện logic đọc một record từ buffer
    }
}
