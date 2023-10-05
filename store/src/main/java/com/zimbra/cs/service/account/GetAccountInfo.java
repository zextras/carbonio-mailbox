// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.account;

import com.google.common.base.Strings;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

/**
 * @author schemers
 */
public class GetAccountInfo extends AccountDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Element a = request.getElement(AccountConstants.E_ACCOUNT);
    String key = a.getAttribute(AccountConstants.A_BY);
    String value = a.getText();

    if (Strings.isNullOrEmpty(value)) {
      throw ServiceException.INVALID_REQUEST(
          "no text specified for the " + AccountConstants.E_ACCOUNT + " element", null);
    }
    Provisioning prov = Provisioning.getInstance();
    Account account = prov.get(AccountBy.fromString(key), value, zsc.getAuthToken());

    // prevent directory harvest attack, mask no such account as permission denied
    if (account == null) throw ServiceException.PERM_DENIED("can not access account");

    Element response = zsc.createElement(AccountConstants.GET_ACCOUNT_INFO_RESPONSE);
    response.addAttribute(AccountConstants.E_NAME, account.getName(), Element.Disposition.CONTENT);
    response.addKeyValuePair(
        Provisioning.A_zimbraId, account.getId(), AccountConstants.E_ATTR, AccountConstants.A_NAME);
    response.addKeyValuePair(
        Provisioning.A_zimbraMailHost,
        account.getAttr(Provisioning.A_zimbraMailHost),
        AccountConstants.E_ATTR,
        AccountConstants.A_NAME);
    response.addKeyValuePair(
        Provisioning.A_displayName,
        account.getAttr(Provisioning.A_displayName),
        AccountConstants.E_ATTR,
        AccountConstants.A_NAME);
    addUrls(response, account);
    return response;
  }

  static void addUrls(Element response, Account account) throws ServiceException {
    Provisioning prov = Provisioning.getInstance();

    Server server = prov.getServer(account);
    String hostname = server.getAttr(Provisioning.A_zimbraServiceHostname);
    Domain domain = prov.getDomain(account);
    if (server != null && hostname != null) {
      String httpSoap = URLUtil.getSoapPublicURL(server, domain, false);
      String httpsSoap = URLUtil.getSoapPublicURL(server, domain, true);

      if (httpSoap != null) {
        response.addAttribute(
            AccountConstants.E_SOAP_URL /* soapURL */, httpSoap, Element.Disposition.CONTENT);
      }
      if (httpsSoap != null && !httpsSoap.equalsIgnoreCase(httpSoap)) {
        /* Note: addAttribute with Element.Disposition.CONTENT REPLACEs any previous attribute with the same name.
         * i.e. Will NOT end up with both httpSoap and httpsSoap as values for "soapURL"
         */
        response.addAttribute(
            AccountConstants.E_SOAP_URL /* soapURL */, httpsSoap, Element.Disposition.CONTENT);
      }
      String pubUrl = URLUtil.getPublicURLForDomain(server, domain, "", true);
      if (pubUrl != null) {
        response.addAttribute(AccountConstants.E_PUBLIC_URL, pubUrl, Element.Disposition.CONTENT);
      }
      if (AccessManager.getInstance().isAdequateAdminAccount(account)) {
        String publicAdminUrl = URLUtil.getPublicAdminConsoleURLForDomain(server, domain);
        if (publicAdminUrl != null) {
          response.addAttribute(
              AccountConstants.E_ADMIN_URL, publicAdminUrl, Element.Disposition.CONTENT);
        }
      }
      String changePasswordUrl = null;
      if (domain != null) {
        changePasswordUrl = domain.getAttr(Provisioning.A_zimbraChangePasswordURL);
      }
      if (changePasswordUrl != null) {
        response.addAttribute(
            AccountConstants.E_CHANGE_PASSWORD_URL, changePasswordUrl, Element.Disposition.CONTENT);
      }
    }

    // add BOSH URL if Chat is enabled
    if (account.getBooleanAttr(Provisioning.A_zimbraFeatureChatEnabled, false)) {
      response.addAttribute(
          AccountConstants.E_BOSH_URL, server.getReverseProxyXmppBoshLocalHttpBindURL());
    }
  }
}
