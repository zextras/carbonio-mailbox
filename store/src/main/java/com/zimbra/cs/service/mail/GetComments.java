// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Comment;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashSet;
import java.util.Map;

public class GetComments extends MailDocumentHandler {

  private static final String[] PARENT_ID_PATH =
      new String[] {MailConstants.E_COMMENT, MailConstants.A_PARENT_ID};

  @Override
  protected String[] getProxiedIdPath(Element request) {
    return PARENT_ID_PATH;
  }

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Mailbox mbox = getRequestedMailbox(zsc);
    OperationContext octxt = getOperationContext(zsc, context);
    ItemIdFormatter ifmt = new ItemIdFormatter(zsc);

    Element c = request.getElement(MailConstants.E_COMMENT);
    String itemId = c.getAttribute(MailConstants.A_PARENT_ID);

    ItemId iid = new ItemId(itemId, zsc);

    Element response = zsc.createElement(MailConstants.GET_COMMENTS_RESPONSE);
    HashSet<Account> accounts = new HashSet<Account>();
    for (Comment comment : mbox.getComments(octxt, iid.getId(), 0, -1)) {
      accounts.add(comment.getCreatorAccount());
      ToXML.encodeComment(response, ifmt, octxt, comment);
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
