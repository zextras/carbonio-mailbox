// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.mailbox.ContactRankings;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class RankingAction extends MailDocumentHandler {

  public static final String OP_RESET = "reset";
  public static final String OP_DELETE = "delete";

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);
    if (account == null) throw AccountServiceException.NO_SUCH_ACCOUNT("");
    Element action = request.getElement(MailConstants.E_ACTION);
    String operation = action.getAttribute(MailConstants.A_OPERATION).toLowerCase();
    if (operation.equals(OP_RESET)) {
      ContactRankings.reset(account.getId());
    } else if (operation.equals(OP_DELETE)) {
      String email = action.getAttribute(MailConstants.A_EMAIL);
      ContactRankings.remove(account.getId(), email);
    }
    return zsc.createElement(MailConstants.RANKING_ACTION_RESPONSE);
  }
}
