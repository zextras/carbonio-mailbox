// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail.message.parser;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * Class encapsulating common data passed among methods.
 */
class ParseMessageContext {

  private final boolean raiseErrorWhenSizeExceed;
  MimeMessageData out;
  ZimbraSoapContext zsc;
  OperationContext octxt;
  Mailbox mbox;
  boolean use2231;
  String defaultCharset;
  long size;
  long maxSize;

  ParseMessageContext() {
    this(true);
  }

  ParseMessageContext(boolean raiseErrorWhenSizeExceed) {
    this.raiseErrorWhenSizeExceed = raiseErrorWhenSizeExceed;
    try {
      Config config = Provisioning.getInstance().getConfig();
      maxSize = config.getLongAttr(Provisioning.A_zimbraMtaMaxMessageSize, -1);
    } catch (ServiceException e) {
      ZimbraLog.soap.warn("Unable to determine max message size.  Disabling limit check.", e);
    }
    if (maxSize < 0) {
      maxSize = Long.MAX_VALUE;
    }
  }

  void incrementSize(String name, long numBytes) throws MailServiceException {
    size += numBytes;
    ZimbraLog.soap.debug("Adding %s, incrementing size by %d to %d.", name, numBytes, size);
    if (raiseErrorWhenSizeExceed && (maxSize != 0 /* 0 means "no limit" */) && (size > maxSize)) {
      throw MailServiceException.MESSAGE_TOO_BIG(maxSize, size);
    }
  }
}
