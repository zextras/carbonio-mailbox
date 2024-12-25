// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.encryption;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.pgp.PgpHandler;
import com.zimbra.cs.smime.SmimeHandler;

import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Optional;

public final class EncryptionHandlerFactory {

    private EncryptionHandlerFactory() {}

    public static Optional<EncryptionHandler> getHandler(MimeMessage mimeMessage) {

        SmimeHandler smimeHandler = SmimeHandler.getHandler();
        PgpHandler pgpHandler = PgpHandler.getHandler();

        if (smimeHandler == null && pgpHandler == null) {
            return Optional.empty();
        }

        try {
            String contentType = mimeMessage.getContentType().toLowerCase(Locale.US);

            if (smimeHandler != null
                && contentType.contains("application/pkcs7-mime")
                && contentType.contains("smime-type=enveloped-data")
                && contentType.contains("name=\"smime.p7m\"")) {
                return Optional.of(smimeHandler);
            }

            if (pgpHandler != null
                    && contentType.contains("multipart/encrypted;")
                    && contentType.contains("protocol=\"application/pgp-encrypted\"")) {
                return Optional.of(pgpHandler);
            }

        } catch (Exception e) {
            ZimbraLog.smime.error("Error getting handler", e);
        }

        return Optional.empty();
    }
}
