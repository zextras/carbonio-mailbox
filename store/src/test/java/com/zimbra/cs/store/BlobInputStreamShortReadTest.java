// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlobInputStreamShortReadTest {

    @TempDir
    Path directory;
    private FileDescriptorCache previousCache;
    private FileDescriptorCache cache;

    @BeforeEach
    void setUp() {
        previousCache = BlobInputStream.getFileDescriptorCache();
        cache = new FileDescriptorCache(null);
        BlobInputStream.setFileDescriptorCache(cache);
    }

    @AfterEach
    void tearDown() {
        cache.shutdown();
        BlobInputStream.setFileDescriptorCache(previousCache);
    }

    @Test
    void returnsOnlyBytesPresentWhenBlobIsShorterThanRecordedSize() throws Exception {
        byte[] content = "stored message".getBytes(StandardCharsets.US_ASCII);
        Path file = directory.resolve("truncated.msg");
        Files.write(file, content);
        byte[] destination = new byte[128];
        Arrays.fill(destination, (byte) '!');

        try (var input = new BlobInputStream(file.toFile(), 100)) {
            assertEquals(content.length, input.read(destination, 3, 100));
            assertArrayEquals(content, Arrays.copyOfRange(destination, 3, 3 + content.length));
            assertEquals('!', destination[3 + content.length]);
            assertEquals(content.length, input.getPosition());
            assertEquals(-1, input.read(destination));
        }
    }

    @Test
    void continuesReadingAfterShortCacheReadsWithoutAddingBytes() throws Exception {
        byte[] content = "message contents split across reads".getBytes(StandardCharsets.US_ASCII);
        Path file = directory.resolve("message.msg");
        Files.write(file, content);
        cache.shutdown();
        cache = new FileDescriptorCache(null) {
            @Override
            public int read(String path, long rawSize, long fileOffset, byte[] bytes, int offset, int length)
                throws java.io.IOException {
                return super.read(path, rawSize, fileOffset, bytes, offset, Math.min(length, 5));
            }
        };
        BlobInputStream.setFileDescriptorCache(cache);

        try (var input = new BlobInputStream(file.toFile(), content.length)) {
            assertArrayEquals(content, input.readNBytes(content.length));
            assertEquals(-1, input.read());
        }
    }
}
