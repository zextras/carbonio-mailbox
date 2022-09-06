// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class DeleteIdentity extends DocumentHandler {

  public Element handle(Element request, Map<String, Object> context)
      throws ServiceException, SoapFaultException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    if (!canModifyOptions(zsc, account))
      throw ServiceException.PERM_DENIED("can not modify options");

    Provisioning prov = Provisioning.getInstance();

    Element eIdentity = request.getElement(AccountConstants.E_IDENTITY);

    // identity can be specified by name or by ID
    Identity ident = null;
    String idStr = eIdentity.getAttribute(AccountConstants.A_ID, null);
    if (idStr != null) {
      ident = prov.get(account, Key.IdentityBy.id, idStr);
    } else {
      idStr = eIdentity.getAttribute(AccountConstants.A_NAME);
      ident = prov.get(account, Key.IdentityBy.name, idStr);
    }

    if (ident != null) Provisioning.getInstance().deleteIdentity(account, ident.getName());
    else throw AccountServiceException.NO_SUCH_IDENTITY(idStr);

    Element response = zsc.createElement(AccountConstants.DELETE_IDENTITY_RESPONSE);
    return response;
  }
}
