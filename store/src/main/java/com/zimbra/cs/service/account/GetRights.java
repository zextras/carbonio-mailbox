// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.accesscontrol.ACLHelper;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GetRights extends AccountDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account)) {
      throw ServiceException.PERM_DENIED("can not access account");
    }

    Set<Right> specificRights = null;
    for (Element eACE : request.listElements(AccountConstants.E_ACE)) {
      if (specificRights == null) specificRights = new HashSet<Right>();
      specificRights.add(
          RightManager.getInstance().getUserRight(eACE.getAttribute(AccountConstants.A_RIGHT)));
    }

    List<ZimbraACE> aces =
        (specificRights == null)
            ? ACLHelper.getAllACEs(account)
            : ACLHelper.getACEs(account, specificRights);
    Element response = zsc.createElement(AccountConstants.GET_RIGHTS_RESPONSE);
    if (aces != null) {
      for (ZimbraACE ace : aces) {
        ToXML.encodeACE(response, ace);
      }
    }
    return response;
  }
}
