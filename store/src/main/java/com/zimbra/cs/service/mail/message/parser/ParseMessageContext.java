// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail.message.parser;

import com.google.common.base.Strings;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 * Class encapsulating common data passed among methods.
 */
class ParseMessageContext {
  private Session smtpSession;
  private boolean raiseErrorWhenSizeExceed;
  private MimeMessageData out;
  private ZimbraSoapContext zsc;
  private OperationContext octxt;
  private Mailbox mbox;
  private boolean use2231;
  private String defaultCharset;
  private long size;
  private long maxSize;

  static ParseMessageContext create(ZimbraSoapContext zsc, OperationContext octxt, Mailbox mbox, MimeMessageData out, boolean raiseErrorWhenSizeExceed)
      throws ServiceException {
    var ctxt = new ParseMessageContext();
    try {
      Config config = Provisioning.getInstance().getConfig();
      ctxt.maxSize = config.getLongAttr(Provisioning.A_zimbraMtaMaxMessageSize, -1);
    } catch (ServiceException e) {
      ZimbraLog.soap.warn("Unable to determine max message size.  Disabling limit check.", e);
    }
    if (ctxt.maxSize < 0) {
      ctxt.maxSize = Long.MAX_VALUE;
    }
    Account target = DocumentHandler.getRequestedAccount(zsc);

    ctxt.out = out;
    ctxt.zsc = zsc;
    ctxt.octxt = octxt;
    ctxt.mbox = mbox;
    ctxt.raiseErrorWhenSizeExceed = raiseErrorWhenSizeExceed;
    ctxt.use2231 = target.isPrefUseRfc2231();
    ctxt.defaultCharset = target.getPrefMailDefaultCharset();
    if (Strings.isNullOrEmpty(ctxt.defaultCharset)) {
      ctxt.defaultCharset = MimeConstants.P_CHARSET_UTF8;
    }
    try {
      ctxt.smtpSession = JMSession.getSmtpSession(target);
    } catch (MessagingException e) {
      throw ServiceException.FAILURE("MessagingException", e);
    }
    return ctxt;
  }

  private ParseMessageContext() {
  }

  void incrementSize(String name, long numBytes) throws MailServiceException {
    size += numBytes;
    ZimbraLog.soap.debug("Adding %s, incrementing size by %d to %d.", name, numBytes, size);
    if (raiseErrorWhenSizeExceed && (maxSize != 0 /* 0 means "no limit" */) && (size > maxSize)) {
      throw MailServiceException.MESSAGE_TOO_BIG(maxSize, size);
    }
  }

  MimeMessageData getOut() {
    return out;
  }

  ZimbraSoapContext getZsc() {
    return zsc;
  }

  OperationContext getOctxt() {
    return octxt;
  }

  Mailbox getMbox() {
    return mbox;
  }

  boolean isUse2231() {
    return use2231;
  }

  String getDefaultCharset() {
    return defaultCharset;
  }

  Session getSmtpSession() {
    return smtpSession;
  }
}
