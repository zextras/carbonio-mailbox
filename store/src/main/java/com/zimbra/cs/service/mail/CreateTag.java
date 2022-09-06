// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.mail;

import com.zimbra.common.mailbox.Color;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailbox.Tag;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

/**
 * @author schemers
 */
public class CreateTag extends MailDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Mailbox mbox = getRequestedMailbox(zsc);
    OperationContext octxt = getOperationContext(zsc, context);
    ItemIdFormatter ifmt = new ItemIdFormatter(zsc);

    Element t = request.getElement(MailConstants.E_TAG);
    String name = t.getAttribute(MailConstants.A_NAME);
    String rgb = t.getAttribute(MailConstants.A_RGB, null);

    Tag tag;
    if (rgb != null) {
      Color color = new Color(rgb);
      tag = mbox.createTag(octxt, name, color);
    } else {
      byte color = (byte) t.getAttributeLong(MailConstants.A_COLOR, MailItem.DEFAULT_COLOR);
      tag = mbox.createTag(octxt, name, color);
    }

    Element response = zsc.createElement(MailConstants.CREATE_TAG_RESPONSE);
    if (tag != null) ToXML.encodeTag(response, ifmt, octxt, tag);
    return response;
  }
}
