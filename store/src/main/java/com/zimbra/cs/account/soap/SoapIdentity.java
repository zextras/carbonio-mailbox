// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;

class SoapIdentity extends Identity implements SoapEntry {

  SoapIdentity(Account acct, String name, String id, Map<String, Object> attrs, Provisioning prov) {
    super(acct, name, id, attrs, prov);
  }

  SoapIdentity(Account acct, com.zimbra.soap.account.type.Identity id, Provisioning prov)
      throws ServiceException {
    super(acct, id.getName(), id.getId(), id.getAttrsAsOldMultimap(), prov);
  }

  SoapIdentity(Account acct, Element e, Provisioning prov) throws ServiceException {
    super(
        acct,
        e.getAttribute(AccountConstants.A_NAME),
        e.getAttribute(AccountConstants.A_ID),
        SoapProvisioning.getAttrs(e, AccountConstants.A_NAME),
        prov);
  }

  public void modifyAttrs(
      SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable)
      throws ServiceException {
    /*
    XMLElement req = new XMLElement(AccountService.MODIFY_IDENTITY_REQUEST);
    Element identity = req.addElement(AccountService.E_IDENTITY);
    identity.addAttribute(AccountService.A_NAME, getName());
    SoapProvisioning.addAttrElements(identity, attrs);
    setAttrs(SoapProvisioning.getAttrs(prov.invoke(req)));
    */
  }

  public void reload(SoapProvisioning prov) throws ServiceException {
    // XMLElement req = new XMLElement(AdminService.GET_ALL_CONFIG_REQUEST);
    // setAttrs(SoapProvisioning.getAttrs(prov.invoke(req)));
  }

  public Account getAccount() throws ServiceException {
    throw ServiceException.INVALID_REQUEST("unsupported, use getAccount(Provisioning)", null);
  }
}
