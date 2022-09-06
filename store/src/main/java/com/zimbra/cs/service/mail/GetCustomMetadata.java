// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.CustomMetadata;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class GetCustomMetadata extends MailDocumentHandler {
  private static final String[] TARGET_ITEM_PATH = new String[] {MailConstants.A_ID};

  @Override
  protected String[] getProxiedIdPath(Element request) {
    return TARGET_ITEM_PATH;
  }

  @Override
  protected boolean checkMountpointProxy(Element request) {
    return false;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Mailbox mbox = getRequestedMailbox(zsc);
    OperationContext octxt = getOperationContext(zsc, context);
    ItemIdFormatter ifmt = new ItemIdFormatter(zsc);

    Element meta = request.getElement(MailConstants.E_METADATA);
    String section = meta.getAttribute(MailConstants.A_SECTION);
    ItemId iid = new ItemId(request.getAttribute(MailConstants.A_ID), zsc);

    MailItem item = mbox.getItemById(octxt, iid.getId(), MailItem.Type.UNKNOWN);
    CustomMetadata custom = item.getCustomData(section);

    Element response = zsc.createElement(MailConstants.GET_METADATA_RESPONSE);
    response.addAttribute(MailConstants.A_ID, ifmt.formatItemId(item));
    ToXML.encodeCustomMetadata(response, custom);
    return response;
  }
}
