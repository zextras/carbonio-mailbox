// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.mime.MimeVisitor;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

// for finding the first text/plain part of a MimeMessage
public class PlainTextFinder extends MimeVisitor {
  private MimePart mPlainTextPart;

  public PlainTextFinder() {}

  public String getPlainText() throws MessagingException, IOException {
    if (mPlainTextPart == null) return null;
    ContentType ct = new ContentType(mPlainTextPart.getContentType());
    String charset = ct.getParameter(MimeConstants.P_CHARSET);
    if (charset == null) charset = MimeConstants.P_CHARSET_DEFAULT;
    byte[] descBytes =
        ByteUtil.getContent(mPlainTextPart.getInputStream(), mPlainTextPart.getSize());
    return new String(descBytes, charset);
  }

  private static boolean matchingType(Part part, String ct) throws MessagingException {
    String mmCtStr = part.getContentType();
    if (mmCtStr != null) {
      ContentType mmCt = new ContentType(mmCtStr);
      return mmCt.match(ct);
    }
    return false;
  }

  @Override
  protected boolean visitBodyPart(MimeBodyPart bp) throws MessagingException {
    if (mPlainTextPart == null && matchingType(bp, MimeConstants.CT_TEXT_PLAIN))
      mPlainTextPart = bp;
    return false;
  }

  @Override
  protected boolean visitMessage(MimeMessage mm, VisitPhase visitKind) throws MessagingException {
    if (mPlainTextPart == null && matchingType(mm, MimeConstants.CT_TEXT_PLAIN))
      mPlainTextPart = mm;
    return false;
  }

  @Override
  protected boolean visitMultipart(MimeMultipart mp, VisitPhase visitKind)
      throws MessagingException {
    return false;
  }
}
