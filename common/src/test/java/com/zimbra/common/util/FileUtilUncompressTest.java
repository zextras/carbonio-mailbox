// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilUncompressTest {

    @TempDir
    Path directory;

    @Test
    void closesSourceWhenGzipHeaderIsInvalid() throws Exception {
        Path source = Files.write(directory.resolve("invalid.gz"), new byte[] {0, 0});
        Path destination = Files.writeString(directory.resolve("destination"), "existing content");
        assertFalse(FileUtil.isGzipped(source.toFile()));

        try (var inputs = mockConstruction(FileInputStream.class,
                (input, context) -> when(input.read()).thenReturn(0))) {
            assertThrows(ZipException.class,
                () -> FileUtil.uncompress(source.toFile(), destination.toFile(), true));
            assertEquals(1, inputs.constructed().size());
            verify(inputs.constructed().get(0)).close();
        }
        assertEquals("existing content", Files.readString(destination));
    }

    @Test
    void uncompressesValidContentWithSyncEnabled() throws Exception {
        byte[] content = "message contents".repeat(100).getBytes(StandardCharsets.UTF_8);
        Path source = directory.resolve("message.gz");
        try (var output = new GZIPOutputStream(Files.newOutputStream(source))) {
            output.write(content);
        }
        Path destination = directory.resolve("message");

        FileUtil.uncompress(source.toFile(), destination.toFile(), true);

        assertArrayEquals(content, Files.readAllBytes(destination));
    }
}
