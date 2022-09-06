// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.type.AccountInfo;
import com.zimbra.soap.admin.type.Attr;
import java.util.Map;

class SoapAccount extends Account implements SoapEntry {

  SoapAccount(String name, String id, Map<String, Object> attrs, Provisioning prov) {
    super(name, id, attrs, null, prov);
  }

  SoapAccount(AccountInfo accountInfo, Provisioning prov) throws ServiceException {
    super(
        accountInfo.getName(),
        accountInfo.getId(),
        Attr.collectionToMap(accountInfo.getAttrList()),
        null,
        prov);
  }

  SoapAccount(Element e, Provisioning prov) throws ServiceException {
    super(
        e.getAttribute(AdminConstants.A_NAME),
        e.getAttribute(AdminConstants.A_ID),
        SoapProvisioning.getAttrs(e),
        null,
        prov);
  }

  public void modifyAttrs(
      SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable)
      throws ServiceException {
    XMLElement req = new XMLElement(AdminConstants.MODIFY_ACCOUNT_REQUEST);
    req.addElement(AdminConstants.E_ID).setText(getId());
    SoapProvisioning.addAttrElements(req, attrs);
    setAttrs(SoapProvisioning.getAttrs(prov.invoke(req).getElement(AdminConstants.E_ACCOUNT)));
  }

  public void reload(SoapProvisioning prov) throws ServiceException {
    XMLElement req = new XMLElement(AdminConstants.GET_ACCOUNT_REQUEST);
    Element a = req.addElement(AdminConstants.E_ACCOUNT);
    a.setText(getId());
    a.addAttribute(AdminConstants.A_BY, AccountBy.id.name());
    setAttrs(SoapProvisioning.getAttrs(prov.invoke(req).getElement(AdminConstants.E_ACCOUNT)));
  }
}
