// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class PurgeRevision extends MailDocumentHandler {

  private static final String[] TARGET_PATH =
      new String[] {MailConstants.E_REVISION, MailConstants.A_ID};

  protected String[] getProxiedIdPath(Element request) {
    return TARGET_PATH;
  }

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element revisionElem = request.getElement(MailConstants.E_REVISION);
    ItemId iid = new ItemId(revisionElem.getAttribute(MailConstants.A_ID), zsc);
    int rev = (int) revisionElem.getAttributeLong(MailConstants.A_VERSION);
    boolean includeOlderRevisions =
        revisionElem.getAttributeBool(MailConstants.A_INCLUDE_OLDER_REVISIONS, false);
    Mailbox mbox = getRequestedMailbox(zsc);
    mbox.purgeRevision(getOperationContext(zsc, context), iid.getId(), rev, includeOlderRevisions);

    return zsc.createElement(MailConstants.PURGE_REVISION_RESPONSE);
  }
}
