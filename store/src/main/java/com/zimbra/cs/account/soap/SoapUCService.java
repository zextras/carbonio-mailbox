// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.UCService;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.admin.type.UCServiceInfo;

class SoapUCService extends UCService implements SoapEntry {

    SoapUCService(String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(name, id, attrs, prov);
    }

    SoapUCService(UCServiceInfo ucServiceInfo, Provisioning prov) throws ServiceException {
        super(ucServiceInfo.getName(), ucServiceInfo.getId(),
                Attr.collectionToMap(ucServiceInfo.getAttrList()), prov);
    }

    SoapUCService(Element e, Provisioning prov) throws ServiceException {
        super(e.getAttribute(AdminConstants.A_NAME), e.getAttribute(AdminConstants.A_ID), 
                SoapProvisioning.getAttrs(e), prov);
    }

    public void modifyAttrs(SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable) 
    throws ServiceException {
        XMLElement req = new XMLElement(AdminConstants.MODIFY_UC_SERVICE_REQUEST);
        req.addElement(AdminConstants.E_ID).setText(getId());
        SoapProvisioning.addAttrElements(req, attrs);
        setAttrs(SoapProvisioning.getAttrs(prov.invoke(req).getElement(AdminConstants.E_UC_SERVICE)));
    }

    public void reload(SoapProvisioning prov) throws ServiceException {
        XMLElement req = new XMLElement(AdminConstants.GET_UC_SERVICE_REQUEST);
        Element a = req.addElement(AdminConstants.E_UC_SERVICE);
        a.setText(getId());
        a.addAttribute(AdminConstants.A_BY, Key.UCServiceBy.id.name());
        setAttrs(SoapProvisioning.getAttrs(prov.invoke(req).getElement(AdminConstants.E_UC_SERVICE)));
    }
}
