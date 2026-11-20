// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.mailbox.Contact;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class VCardPhotoTest {

    private static final String PHOTO =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+j3ioAAAAASUVORK5CYII=";

    @Test
    void importsPhotoWithoutType() throws Exception {
        Contact.Attachment attachment = parsePhoto("");
        assertEquals("application/octet-stream", attachment.getContentType());
        assertPhotoBytes(attachment);
    }

    @Test
    void preservesExplicitPhotoType() throws Exception {
        Contact.Attachment attachment = parsePhoto(";TYPE=PNG");
        assertEquals("image/png", attachment.getContentType());
        assertPhotoBytes(attachment);
    }

    private Contact.Attachment parsePhoto(String type) throws Exception {
        String card = "BEGIN:VCARD\r\nVERSION:3.0\r\nFN:Jane Doe\r\n"
            + "PHOTO;ENCODING=b" + type + ":" + PHOTO + "\r\nEND:VCARD\r\n";
        var cards = VCard.parseVCard(card);
        assertEquals(1, cards.size());
        assertEquals(1, cards.get(0).attachments.size());
        return cards.get(0).attachments.get(0);
    }

    private void assertPhotoBytes(Contact.Attachment attachment) throws Exception {
        assertEquals("image", attachment.getName());
        try (var input = attachment.getInputStream()) {
            assertArrayEquals(Base64.getDecoder().decode(PHOTO), input.readAllBytes());
        }
    }
}
