// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.RuleManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.GetFilterRulesResponse;
import java.util.Map;

public final class GetFilterRules extends MailDocumentHandler {

  @Override
  public Element handle(Element req, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account)) {
      throw ServiceException.PERM_DENIED("cannot access account");
    }

    GetFilterRulesResponse resp = new GetFilterRulesResponse();
    resp.addFilterRule(RuleManager.getIncomingRulesAsXML(account));
    return zsc.jaxbToElement(resp);
  }
}
