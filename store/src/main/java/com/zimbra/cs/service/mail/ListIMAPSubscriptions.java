// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.ListIMAPSubscriptionsResponse;
import java.util.Map;
import java.util.Set;

public class ListIMAPSubscriptions extends MailDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account)) {
      throw ServiceException.PERM_DENIED("can not access account");
    }

    Set<String> subs =
        AccountUtil.parseConfig(
            getRequestedMailbox(zsc)
                .getConfig(getOperationContext(zsc, context), AccountUtil.SN_IMAP));
    ListIMAPSubscriptionsResponse resp = new ListIMAPSubscriptionsResponse();
    resp.setSubscriptions(subs);
    return zsc.jaxbToElement(resp);
  }
}
