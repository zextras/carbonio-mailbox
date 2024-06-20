// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.util.ZimbraLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import net.freeutils.tnef.TNEFUtils;

/**
 * The TnefConverter class is a MimeVisitor that ensures all application/ms-tnef
 * attachments are handled as a single part without expanding their content.
 * @author bburtin

 */
public class TnefConverter extends MimeVisitor {

    @Override
    protected boolean visitBodyPart(MimeBodyPart bp) {
        return false;
    }

    /**
     * Visits a MimeMessage. This method checks if the message contains TNEF content
     * and ensures it is handled as a single part without expansion.
     *
     * @param msg the MimeMessage to visit
     * @param visitKind the VisitPhase indicating the stage of the visit
     * @return false indicating no further processing is required
     * @throws MessagingException if an error occurs while processing the message
     */
    @Override
    protected boolean visitMessage(MimeMessage msg, VisitPhase visitKind) throws MessagingException {
        if (visitKind == VisitPhase.VISIT_BEGIN) {
            return false;
        }

        try {
            if (!TNEFUtils.isTNEFMimeType(msg.getContentType())) {
                return false;
            }

            if (mCallback != null && !mCallback.onModification()) {
                return false;
            }

            Object content = msg.getContent();
            if (!(content instanceof MimeBodyPart)) {
                return false;
            }

            return false;
        } catch (MessagingException | IOException e) {
            ZimbraLog.extensions.warn("Exception while processing TNEF message content; skipping part", e);
            return false;
        }
    }

    /**
     * Visits a MimeMultipart. This method checks for TNEF parts and ensures they
     * are handled as single parts without expansion.
     *
     * @param mmp the MimeMultipart to visit
     * @param visitKind the VisitPhase indicating the stage of the visit
     * @return true if TNEF parts were found and handled; false otherwise
     * @throws MessagingException if an error occurs while processing the multipart
     */
    @Override
    protected boolean visitMultipart(MimeMultipart mmp, VisitPhase visitKind) throws MessagingException {
        if (visitKind != VisitPhase.VISIT_END) {
            return false;
        }

        if (MimeConstants.CT_MULTIPART_ALTERNATIVE.equals(mmp.getContentType())) {
            return false;
        }

        try {
            boolean found = false;
            for (int i = 0; i < mmp.getCount() && !found; i++) {
                BodyPart bp = mmp.getBodyPart(i);
                if (bp instanceof MimeBodyPart && TNEFUtils.isTNEFMimeType(bp.getContentType())) {
                    found = true;
                }
            }
            if (!found) {
                return false;
            }

            if (mCallback != null && !mCallback.onModification()) {
                return false;
            }
        } catch (MessagingException e) {
            ZimbraLog.extensions.warn("Exception while traversing multipart; skipping", e);
            return false;
        }

        // Collect TNEF parts to be left unchanged
        List<Integer> tnefPartsIndices = new ArrayList<>();
        try {
            for (int i = 0; i < mmp.getCount(); i++) {
                BodyPart bp = mmp.getBodyPart(i);
                if (bp instanceof MimeBodyPart && TNEFUtils.isTNEFMimeType(bp.getContentType())) {
                    // Skip expansion for TNEF parts and leave them as they are
                    tnefPartsIndices.add(i);
                }
            }
        } catch (MessagingException e) {
            ZimbraLog.extensions.warn("Exception while traversing multipart; skipping", e);
            return false;
        }

        return !tnefPartsIndices.isEmpty();
    }
}
