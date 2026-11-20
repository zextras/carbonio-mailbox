// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlobBuilderFinishTest {

    @TempDir
    Path directory;
    private static final byte[] CONTENT = "mail body and attachment data ".repeat(100)
        .getBytes(StandardCharsets.UTF_8);

    @Test
    void propagatesFailureWritingCompressedDataAtFinish() throws Exception {
        Blob blob = new Blob(directory.resolve("failed.msg").toFile());
        TestBlobBuilder builder = new TestBlobBuilder(blob, true);
        builder.append(CONTENT);
        builder.output.failWrites = true;

        try (var storeManager = mockStatic(StoreManager.class)) {
            StoreManager store = mock(StoreManager.class);
            storeManager.when(StoreManager::getInstance).thenReturn(store);
            IOException error = assertThrows(IOException.class, builder::finish);
            assertEquals("disk write failed", error.getMessage());
            verify(store).quietDelete(blob);
        }
    }

    @Test
    void completesCompressedDataBeforeFlushingAndSyncing() throws Exception {
        Blob blob = new Blob(directory.resolve("compressed.msg").toFile());
        TestBlobBuilder builder = new TestBlobBuilder(blob, true);
        builder.append(CONTENT);
        assertSame(blob, builder.finish());

        try (var input = new GZIPInputStream(new ByteArrayInputStream(builder.output.bytesAtFlush))) {
            assertArrayEquals(CONTENT, input.readAllBytes());
        }
        assertEquals(CONTENT.length, blob.getRawSize());
        assertTrue(blob.isCompressed());
    }

    @Test
    void finishesUncompressedData() throws Exception {
        Blob blob = new Blob(directory.resolve("plain.msg").toFile());
        TestBlobBuilder builder = new TestBlobBuilder(blob, false);
        builder.append(CONTENT);
        assertSame(blob, builder.finish());
        assertArrayEquals(CONTENT, Files.readAllBytes(blob.getFile().toPath()));
        assertFalse(blob.isCompressed());
    }

    private static class TestBlobBuilder extends BlobBuilder {
        private final boolean compress;
        private TestOutputStream output;

        TestBlobBuilder(Blob blob, boolean compress) {
            super(blob);
            this.compress = compress;
        }

        @Override
        protected boolean useCompression() {
            return compress;
        }

        @Override
        protected OutputStream createOutputStream(File file) throws FileNotFoundException {
            output = new TestOutputStream(file);
            return output;
        }
    }

    private static class TestOutputStream extends FileOutputStream {
        private final File file;
        private boolean failWrites;
        private byte[] bytesAtFlush;

        TestOutputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            if (failWrites) {
                throw new IOException("disk write failed");
            }
            super.write(bytes, offset, length);
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            bytesAtFlush = Files.readAllBytes(file.toPath());
        }
    }
}
