// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.soap.admin.message.GetAllConfigResponse;
import com.zimbra.soap.admin.message.GetConfigResponse;
import com.zimbra.soap.admin.type.Attr;

class SoapConfig extends Config implements SoapEntry {
    
    SoapConfig(Map<String, Object> attrs, Provisioning provisioning) {
        super(attrs, provisioning);
    }

    SoapConfig(GetAllConfigResponse resp, Provisioning provisioning)
    throws ServiceException {
        super(Attr.collectionToMap(resp.getAttrs()), provisioning);
    }
    
    SoapConfig(GetConfigResponse resp, Provisioning provisioning)
    throws ServiceException {
        super(Attr.collectionToMap(resp.getAttrs()), provisioning);
    }

    SoapConfig(Element e, Provisioning provisioning) throws ServiceException {
        super(SoapProvisioning.getAttrs(e), provisioning);
    }

    public void modifyAttrs(SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable) throws ServiceException {
        XMLElement req = new XMLElement(AdminConstants.MODIFY_CONFIG_REQUEST);
        SoapProvisioning.addAttrElements(req, attrs);
        setAttrs(SoapProvisioning.getAttrs(prov.invoke(req)));
    }

    public void reload(SoapProvisioning prov) throws ServiceException {
        XMLElement req = new XMLElement(AdminConstants.GET_ALL_CONFIG_REQUEST);
        setAttrs(SoapProvisioning.getAttrs(prov.invoke(req)));
    }
}
