// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public abstract class AdminGalDocumentHandler extends AdminDocumentHandler {
  private static final String[] TARGET_ACCOUNT_PATH =
      new String[] {AccountConstants.A_GAL_ACCOUNT_ID};

  protected String[] getProxiedAccountPath() {
    return TARGET_ACCOUNT_PATH;
  }

  @Override
  protected Element proxyIfNecessary(Element request, Map<String, Object> context)
      throws ServiceException {
    try {
      ZimbraSoapContext zsc = getZimbraSoapContext(context);

      Provisioning prov = Provisioning.getInstance();

      // check whether we need to proxy to the home server of the GAL sync account
      String[] xpath = getProxiedAccountPath();
      String acctId = (xpath != null ? getXPath(request, xpath) : null);
      if (acctId != null) {
        Account acct = prov.get(AccountBy.id, acctId, zsc.getAuthToken());
        if (acct != null) {
          if (!Provisioning.getInstance().onLocalServer(acct)) {
            /*
             * bug 69805, see comments in com.zimbra.cs.service.account.GalDocumentHandler
             */
            boolean proxied =
                request.getAttributeBool(AccountConstants.A_GAL_ACCOUNT_PROXIED, false);
            if (proxied) {
              /*
               * This is the rolling upgrade path, when galsync account is
               * on a pre-7.x server and this is a 7.x-or-later server.
               * Just do the pre-7.x behavior for <authToken> and <account>
               * in soap header.
               */
              return proxyRequest(request, context, AuthProvider.getAdminAuthToken(), acctId);
            } else {
              /*
               * normal path
               */
              Server server = acct.getServer();
              return proxyRequest(request, context, server);
            }
          } else {
            // galAcctId is on local server
            return null;
          }
        }
      }

      // galAcctId is not present, or is present but not found(should throw?)
      return super.proxyIfNecessary(request, context);
    } catch (ServiceException e) {
      // if something went wrong proxying the request, just execute it locally
      if (ServiceException.PROXY_ERROR.equals(e.getCode())) {
        return null;
      }
      // but if it's a real error, it's a real error
      throw e;
    }
  }
}
