// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class CreateIdentity extends DocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    if (!canModifyOptions(zsc, account)) {
      throw ServiceException.PERM_DENIED("can not modify options");
    }

    Element identityEl = request.getElement(AccountConstants.E_IDENTITY);
    String name = identityEl.getAttribute(AccountConstants.A_NAME);
    Map<String, Object> attrs = AccountService.getAttrs(identityEl, true, AccountConstants.A_NAME);
    Identity identity = Provisioning.getInstance().createIdentity(account, name, attrs);

    Element response = zsc.createElement(AccountConstants.CREATE_IDENTITY_RESPONSE);
    ToXML.encodeIdentity(response, identity);
    return response;
  }
}
