// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.zmime.ZMimeBodyPart;
import com.zimbra.common.zmime.ZMimeMultipart;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.util.JMSession;
import java.io.IOException;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public final class MailboxTestUtil {

  private MailboxTestUtil() {}

  public static void index(Mailbox mbox) throws ServiceException {
    mbox.index.indexDeferredItems();
  }

  public static ParsedMessage generateMessage(String subject) throws Exception {
    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
    mm.setHeader("From", "Bob Evans <bob@example.com>");
    mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
    mm.setHeader("Subject", subject);
    mm.setText("nothing to see here");
    return new ParsedMessage(mm, false);
  }

  public static ParsedMessage generateMessageWithAttachment(String subject) throws Exception {
    MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession());
    mm.setHeader("From", "Vera Oliphant <oli@example.com>");
    mm.setHeader("To", "Jimmy Dean <jdean@example.com>");
    mm.setHeader("Subject", subject);
    mm.setText("Good as gold");
    MimeMultipart multi = new ZMimeMultipart("mixed");
    ContentDisposition cdisp = new ContentDisposition(Part.ATTACHMENT);
    cdisp.setParameter("filename", "fun.txt");

    ZMimeBodyPart bp = new ZMimeBodyPart();
    // MimeBodyPart.setDataHandler() invalidates Content-Type and CTE if there is any, so make sure
    // it gets called before setting Content-Type and CTE headers.
    try {
      bp.setDataHandler(
          new DataHandler(new ByteArrayDataSource("Feeling attached.", "text/plain")));
    } catch (IOException e) {
      throw new MessagingException("could not generate mime part content", e);
    }
    bp.addHeader("Content-Disposition", cdisp.toString());
    bp.addHeader("Content-Type", "text/plain");
    bp.addHeader("Content-Transfer-Encoding", MimeConstants.ET_8BIT);
    multi.addBodyPart(bp);

    mm.setContent(multi);
    mm.saveChanges();

    return new ParsedMessage(mm, false);
  }

}
