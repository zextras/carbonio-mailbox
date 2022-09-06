// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Iterator;
import java.util.Map;

public class ModifyIdentity extends DocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);
    Identity identity = null;

    if (!canModifyOptions(zsc, account))
      throw ServiceException.PERM_DENIED("can not modify options");

    Provisioning prov = Provisioning.getInstance();

    Element eIdentity = request.getElement(AccountConstants.E_IDENTITY);
    Map<String, Object> attrs = AccountService.getAttrs(eIdentity, AccountConstants.A_NAME);

    // remove anything that doesn't start with zimbraPref. ldap will also do additional checks
    for (Iterator<String> it = attrs.keySet().iterator(); it.hasNext(); )
      if (!it.next()
          .toLowerCase()
          .startsWith(
              "zimbrapref")) // if this changes, make sure we don't let them ever change objectclass
      it.remove();

    String key, id = eIdentity.getAttribute(AccountConstants.A_ID, null);
    if (id != null) {
      identity = prov.get(account, Key.IdentityBy.id, key = id);
    } else {
      identity =
          prov.get(
              account, Key.IdentityBy.name, key = eIdentity.getAttribute(AccountConstants.A_NAME));
    }

    if (identity == null) {
      String[] childIds = account.getChildAccount();
      for (String childId : childIds) {
        Account childAccount = prov.get(AccountBy.id, childId, zsc.getAuthToken());
        if (childAccount != null) {
          Identity childIdentity;

          if (id != null) {
            childIdentity = prov.get(childAccount, Key.IdentityBy.id, key = id);
          } else {
            childIdentity =
                prov.get(
                    childAccount,
                    Key.IdentityBy.name,
                    key = eIdentity.getAttribute(AccountConstants.A_NAME));
          }

          if (childIdentity != null) {
            identity = childIdentity;
            account = childAccount;
            break;
          }
        }
      }
    }

    if (identity == null) throw AccountServiceException.NO_SUCH_IDENTITY(key);

    prov.modifyIdentity(account, identity.getName(), attrs);

    Element response = zsc.createElement(AccountConstants.MODIFY_IDENTITY_RESPONSE);
    return response;
  }
}
