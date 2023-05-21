// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


/**
 * <code>MimeVisitor</code> implementation used for printing
 * the structure of a <code>MimePart</code>.  Used for debugging.
 * 
 * @author bburtin
 */
public class MimeFormatter
extends MimeVisitor {

    private int mIndentLevel = 0;
    private StringBuilder mBuf = new StringBuilder(); 
    
    @Override public boolean visitMessage(MimeMessage mm, VisitPhase visitPhase)
    throws MessagingException {
        if (visitPhase == VisitPhase.VISIT_BEGIN) {
            indent();
            mBuf.append("Message: ").append(Mime.getSubject(mm)).append("\n");
            mIndentLevel++;
        } else {
            mIndentLevel--;
        }
        return false;
    }

    @Override public boolean visitMultipart(MimeMultipart mp, VisitPhase visitPhase) {
        if (visitPhase == VisitPhase.VISIT_BEGIN) {
            indent();
            mBuf.append("Multipart:\n");
            mIndentLevel++;
        } else {
            mIndentLevel--;
        }
        return false;
    }

    @Override public boolean visitBodyPart(MimeBodyPart bp) throws MessagingException {
        indent();
        mBuf.append("Part: type=").append(bp.getContentType()).append(", filename=")
            .append(bp.getFileName()).append("\n");
        if (bp.getContentType().startsWith("text")) {
            mBuf.append("----------\n");
            try {
                mBuf.append(bp.getContent());
            } catch (IOException e) {
                mBuf.append(e).append("\n");
            }
            mBuf.append("----------\n");
        }
        return false;
    }

    private void indent() {
      mBuf.append("  ".repeat(Math.max(0, mIndentLevel)));
    }

    @Override public String toString() {
        return mBuf.toString();
    }
}
