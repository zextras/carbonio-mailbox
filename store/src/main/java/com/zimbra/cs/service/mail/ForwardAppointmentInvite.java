// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.calendar.ZCalendar.ZCalendarBuilder;
import com.zimbra.common.calendar.ZCalendar.ZComponent;
import com.zimbra.common.calendar.ZCalendar.ZVCalendar;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.Message.CalendarItemInfo;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.mime.MimeProcessor;
import com.zimbra.cs.mime.MimeProcessorUtil;
import com.zimbra.cs.mime.MimeVisitor;
import com.zimbra.cs.service.mail.message.parser.MimeMessageData;
import com.zimbra.cs.service.mail.message.parser.ParseMimeMessage;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class ForwardAppointmentInvite extends ForwardAppointment {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account senderAcct = getAuthenticatedAccount(zsc);
    Mailbox mbox = getRequestedMailbox(zsc);
    OperationContext octxt = getOperationContext(zsc, context);

    // proxy handling

    ItemId iid = new ItemId(request.getAttribute(MailConstants.A_ID), zsc);
    if (!iid.belongsTo(getRequestedAccount(zsc))) {
      // Proxy it.
      return proxyRequest(request, context, iid.getAccountId());
    }

    Element msgElem = request.getElement(MailConstants.E_MSG);
    MimeMessageData parsedMessageData = new MimeMessageData();
    MimeMessage mmFwdWrapper =
        ParseMimeMessage.parseMimeMsgSoap(
            zsc,
            octxt,
            mbox,
            msgElem,
            null,
            ParseMimeMessage.NO_INV_ALLOWED_PARSER,
            parsedMessageData);

    Message msg = mbox.getMessageById(octxt, iid.getId());
    if (msg == null) {
      throw MailServiceException.NO_SUCH_MSG(iid.getId());
    }

    final MimeMessage message = getMessagePair(mbox, senderAcct, msg, mmFwdWrapper);
    MimeProcessor mimeProcessor = MimeProcessorUtil.getMimeProcessor(request, context);
    forwardMessages(mbox, octxt, message, mimeProcessor);

    Element response = getResponseElement(zsc);
    return response;
  }

  public static void forwardMessages(
          Mailbox mbox, OperationContext octxt, MimeMessage message, MimeProcessor mimeProcessor)
      throws ServiceException {
    if (message != null) {
      sendFwdMsg(octxt, mbox, message, mimeProcessor);
    }
  }

  public static MimeMessage getMessagePair(
      Mailbox mbox, Account senderAcct, Message msg, MimeMessage mmFwdWrapper)
      throws ServiceException {
    MimeMessage mimeMsg;
    mbox.lock.lock();
    try {
      MimeMessage mmInv = msg.getMimeMessage();
      List<Invite> invs = new ArrayList<>();
      for (Iterator<CalendarItemInfo> iter = msg.getCalendarItemInfoIterator(); iter.hasNext(); ) {
        CalendarItemInfo cii = iter.next();
        Invite inv = cii.getInvite();
        if (inv != null) {
          invs.add(inv);
        }
      }
      ZVCalendar cal = null;
      Invite firstInv = null;
      if (!invs.isEmpty()) {
        // Recreate the VCALENDAR from Invites.
        boolean first = true;
        for (Invite inv : invs) {
          if (first) {
            first = false;
            firstInv = inv;
            cal = inv.newToICalendar(true);
          } else {
            ZComponent comp = inv.newToVComponent(true, true);
            cal.addComponent(comp);
          }
        }
      } else {
        // If no invites found in metadata, parse from text/calendar MIME part.
        try {
          CalPartDetectVisitor visitor = new CalPartDetectVisitor();
          visitor.accept(mmInv);
          MimeBodyPart calPart = visitor.getCalendarPart();
          if (calPart != null) {
            String ctHdr = calPart.getContentType();
            ContentType ct = new ContentType(ctHdr);
            String charset = ct.getParameter(MimeConstants.P_CHARSET);
            if (charset == null || charset.length() == 0) charset = MimeConstants.P_CHARSET_UTF8;
            InputStream is = calPart.getInputStream();
            try {
              cal = ZCalendarBuilder.build(is, charset);
            } finally {
              ByteUtil.closeStream(is);
            }
            List<Invite> invList =
                Invite.createFromCalendar(senderAcct, msg.getFragment(), cal, false);
            if (invList != null && !invList.isEmpty()) firstInv = invList.get(0);
            if (firstInv == null)
              throw ServiceException.FAILURE(
                  "Error building Invite for calendar part in message " + msg.getId(), null);
          }
        } catch (MessagingException | IOException e) {
          throw ServiceException.FAILURE(
              "Error getting calendar part in message " + msg.getId(), null);
        }
      }

      mimeMsg = getInstanceFwdMsg(senderAcct, firstInv, cal, mmInv, mmFwdWrapper);

    } finally {
      mbox.lock.release();
    }
    return mimeMsg;
  }

  // MimeVisitor that finds text/calendar part.
  private static class CalPartDetectVisitor extends MimeVisitor {
    private MimeBodyPart mCalPart;

    public CalPartDetectVisitor() {}

    public MimeBodyPart getCalendarPart() {
      return mCalPart;
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
      if (mCalPart == null && matchingType(bp, MimeConstants.CT_TEXT_CALENDAR)) mCalPart = bp;
      return false;
    }

    @Override
    protected boolean visitMessage(MimeMessage mm, VisitPhase visitKind) throws MessagingException {
      return false;
    }

    @Override
    protected boolean visitMultipart(MimeMultipart mp, VisitPhase visitKind)
        throws MessagingException {
      return false;
    }
  }
}
