// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class GetTag extends MailDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Mailbox mbox = getRequestedMailbox(zsc);
    OperationContext octxt = getOperationContext(zsc, context);
    ItemIdFormatter ifmt = new ItemIdFormatter(zsc);

    List<Tag> tags = null;
    try {
      tags = mbox.getTagList(octxt);
    } catch (ServiceException e) {
      // just return no tags in the perm denied case (not considered an error here)...
      if (!e.getCode().equals(ServiceException.PERM_DENIED)) throw e;
    }

    Element response = zsc.createElement(MailConstants.GET_TAG_RESPONSE);
    if (tags != null) {
      for (Tag tag : tags) {
        if (tag == null || tag instanceof Flag) continue;
        ToXML.encodeTag(response, ifmt, octxt, tag);
      }
    }
    return response;
  }
}
