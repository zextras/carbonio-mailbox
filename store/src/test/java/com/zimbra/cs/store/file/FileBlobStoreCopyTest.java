// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.volume.Volume;
import com.zimbra.cs.volume.VolumeManager;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileBlobStoreCopyTest extends MailboxTestSuite {

    @TempDir
    Path directory;

    @Test
    void equalLengthGzipCopyCanBeReadUsingTheRecordedMessageSize() throws Exception {
        byte[] content = ("Subject: Evidence\r\n\r\n" + "x".repeat(22))
            .getBytes(StandardCharsets.US_ASCII);
        var gzipBytes = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(gzipBytes)) {
            gzip.write(content);
        }
        assertEquals(content.length, gzipBytes.size(), "fixture must compress to its original size");

        FileBlobStore store = (FileBlobStore) StoreManager.getInstance();
        VolumeManager volumes = VolumeManager.getInstance();
        var mailbox = MailboxManager.getInstance().getMailboxByAccount(createAccount().create());
        Blob source = new VolumeBlob(Files.write(directory.resolve("source.msg"), content).toFile(),
            volumes.getCurrentMessageVolume().getId())
            .setRawSize(content.length)
            .setCompressed(false);
        Volume destination = volumes.create(Volume.builder()
            .setType(Volume.TYPE_MESSAGE_SECONDARY)
            .setName("compressed-copy")
            .setPath(Files.createDirectory(directory.resolve("destination")).toString(), true)
            .setCompressBlobs(true)
            .setCompressionThreshold(0)
            .build(), true);
        try {
            store.copy(source, mailbox, 42, 1, destination.getId());
            var reloaded = store.getMailboxBlob(mailbox, 42, 1, destination.getLocator())
                .setSize(content.length);
            try (var input = store.getContent(reloaded)) {
                assertArrayEquals(content, input.readAllBytes());
            }
            assertFalse(reloaded.getLocalBlob().isCompressed());
        } finally {
            volumes.delete(destination.getId(), true);
        }
    }
}
