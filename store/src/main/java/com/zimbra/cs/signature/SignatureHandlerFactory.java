package com.zimbra.cs.signature;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.pgp.PgpHandler;
import com.zimbra.cs.smime.SmimeHandler;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Optional;

public final class SignatureHandlerFactory {

    private SignatureHandlerFactory() {}

    public static Optional<SignatureHandler> getHandler(MimeMessage mimeMessage) {
        if (SmimeHandler.getHandler() == null && PgpHandler.getHandler() == null) {
            return Optional.empty();
        }

        try {
            Object content = mimeMessage.getContent();

            if (content instanceof Multipart multipart) {
                int partCount = multipart.getCount();

                if (partCount <= 0) {
                    return Optional.empty();
                }

                BodyPart lastPart = multipart.getBodyPart(partCount - 1);
                if (lastPart == null || lastPart.getContentType() == null) {
                    return Optional.empty();
                }

                if (SmimeHandler.getHandler() != null
                        && (lastPart.getContentType().toLowerCase(Locale.US).contains(MimeConstants.CT_APPLICATION_SMIME_SIGNATURE)
                        || lastPart.getContentType().toLowerCase(Locale.US).contains(MimeConstants.CT_APPLICATION_SMIME)
                        || lastPart.getContentType().toLowerCase(Locale.US).contains(MimeConstants.CT_APPLICATION_SMIME_OLD))) {
                    return Optional.of(SmimeHandler.getHandler());

                }

                if (PgpHandler.getHandler() != null
                        && lastPart.getContentType().toLowerCase(Locale.US).contains("application/pgp-signature")) {
                    return Optional.of(PgpHandler.getHandler());
                }

            }

        } catch (Exception e) {
            ZimbraLog.smime.error("Error getting handler", e);
        }

        return Optional.empty();
    }
}
