// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.volume.VolumeManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FileBlobStoreReadFailureTest extends MailboxTestSuite {

    @Test
    void failedIncomingReadDoesNotLeaveAPartialBlob() throws Exception {
        Path incoming = Path.of(VolumeManager.getInstance().getCurrentMessageVolume()
            .getIncomingDirectory().getPath());
        Files.createDirectories(incoming);
        long filesBefore;
        try (var files = Files.list(incoming)) {
            filesBefore = files.count();
        }
        IOException readFailure = new IOException("message stream interrupted");
        byte[] firstChunk = "Subject: Evidence\r\n\r\npartial body".getBytes(StandardCharsets.US_ASCII);
        try (InputStream input = new SequenceInputStream(new ByteArrayInputStream(firstChunk),
                new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw readFailure;
                    }
                })) {
            assertSame(readFailure, assertThrows(IOException.class,
                () -> StoreManager.getInstance().storeIncoming(input)));
        }
        try (var files = Files.list(incoming)) {
            assertEquals(filesBefore, files.count(), "failed delivery must not leave an incoming blob");
        }
    }
}
