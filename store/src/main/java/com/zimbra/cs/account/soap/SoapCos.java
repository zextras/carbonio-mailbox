// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.admin.type.CosInfo;
import java.util.Map;

class SoapCos extends Cos implements SoapEntry {

  SoapCos(String name, String id, Map<String, Object> attrs, Provisioning prov) {
    super(name, id, attrs, prov);
  }

  SoapCos(CosInfo cosInfo, Provisioning prov) throws ServiceException {
    super(cosInfo.getName(), cosInfo.getId(), Attr.collectionToMap(cosInfo.getAttrList()), prov);
  }

  SoapCos(Element e, Provisioning prov) throws ServiceException {
    super(
        e.getAttribute(AdminConstants.A_NAME),
        e.getAttribute(AdminConstants.A_ID),
        SoapProvisioning.getAttrs(e),
        prov);
  }

  public void modifyAttrs(
      SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable)
      throws ServiceException {
    XMLElement req = new XMLElement(AdminConstants.MODIFY_COS_REQUEST);
    req.addElement(AdminConstants.E_ID).setText(getId());
    SoapProvisioning.addAttrElements(req, attrs);
    setAttrs(SoapProvisioning.getAttrs(prov.invoke(req).getElement(AdminConstants.E_COS)));
  }

  public void reload(SoapProvisioning prov) throws ServiceException {
    XMLElement req = new XMLElement(AdminConstants.GET_COS_REQUEST);
    Element a = req.addElement(AdminConstants.E_COS);
    a.setText(getId());
    a.addAttribute(AdminConstants.A_BY, Key.CosBy.id.name());
    setAttrs(SoapProvisioning.getAttrs(prov.invoke(req).getElement(AdminConstants.E_COS)));
  }
}
