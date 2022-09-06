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
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

/**
 * @since May 26, 2004
 * @author schemers
 */
public final class GetIdentities extends AccountDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account)) {
      throw ServiceException.PERM_DENIED("can not access account");
    }

    Element response = zsc.createElement(AccountConstants.GET_IDENTITIES_RESPONSE);
    Provisioning prov = Provisioning.getInstance();
    for (Identity ident : prov.getAllIdentities(account)) {
      ToXML.encodeIdentity(response, ident);
    }
    return response;
  }
}
