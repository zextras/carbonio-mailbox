// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.doc;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Document;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ToXML;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashSet;
import java.util.Map;

public class ListDocumentRevisions extends DocDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Mailbox mbox = getRequestedMailbox(zsc);
    OperationContext octxt = getOperationContext(zsc, context);
    ItemIdFormatter ifmt = new ItemIdFormatter(zsc);

    Element doc = request.getElement(MailConstants.E_DOC);
    String id = doc.getAttribute(MailConstants.A_ID);
    int version = (int) doc.getAttributeLong(MailConstants.A_VERSION, -1);
    int count = (int) doc.getAttributeLong(MailConstants.A_COUNT, 1);

    Element response = zsc.createElement(MailConstants.LIST_DOCUMENT_REVISIONS_RESPONSE);

    Document item;

    ItemId iid = new ItemId(id, zsc);
    item = mbox.getDocumentById(octxt, iid.getId());

    if (version < 0) {
      version = item.getVersion();
    }
    MailItem.Type type = item.getType();
    HashSet<Account> accounts = new HashSet<Account>();
    Provisioning prov = Provisioning.getInstance();
    while (version > 0 && count > 0) {
      item = (Document) mbox.getItemRevision(octxt, iid.getId(), type, version);
      if (item != null) {
        ToXML.encodeDocument(response, ifmt, octxt, item);
        Account a = prov.getAccountByName(item.getCreator());
        if (a != null) accounts.add(a);
      }
      version--;
      count--;
    }
    for (Account a : accounts) {
      Element user = response.addElement(MailConstants.A_USER);
      user.addAttribute(MailConstants.A_ID, a.getId());
      user.addAttribute(MailConstants.A_EMAIL, a.getName());
      user.addAttribute(MailConstants.A_NAME, a.getDisplayName());
    }

    return response;
  }
}
