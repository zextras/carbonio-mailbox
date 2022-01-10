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
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.cs.account.AlwaysOnCluster;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.admin.type.AlwaysOnClusterInfo;
import com.zimbra.soap.admin.type.Attr;

class SoapAlwaysOnCluster extends AlwaysOnCluster implements SoapEntry {

    SoapAlwaysOnCluster(String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(name, id, attrs, null, prov);
    }

    SoapAlwaysOnCluster(AlwaysOnClusterInfo clusterInfo, Provisioning prov) throws ServiceException {
        super(clusterInfo.getName(), clusterInfo.getId(),
                Attr.collectionToMap(clusterInfo.getAttrList()), null, prov);
    }

    SoapAlwaysOnCluster(Element e, Provisioning prov) throws ServiceException {
        super(e.getAttribute(AdminConstants.A_NAME), e.getAttribute(AdminConstants.A_ID), SoapProvisioning.getAttrs(e), null, prov);
    }

    @Override
    public void modifyAttrs(SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable) throws ServiceException {
        XMLElement req = new XMLElement(AdminConstants.MODIFY_ALWAYSONCLUSTER_REQUEST);
        req.addElement(AdminConstants.E_ID).setText(getId());
        SoapProvisioning.addAttrElements(req, attrs);
        setAttrs(SoapProvisioning.getAttrs(prov.invoke(req).getElement(AdminConstants.E_ALWAYSONCLUSTER)));
    }

    @Override
    public void reload(SoapProvisioning prov) throws ServiceException {
        XMLElement req = new XMLElement(AdminConstants.GET_ALWAYSONCLUSTER_REQUEST);
        Element a = req.addElement(AdminConstants.E_ALWAYSONCLUSTER);
        a.setText(getId());
        a.addAttribute(AdminConstants.A_BY, Key.AlwaysOnClusterBy.id.name());
        setAttrs(SoapProvisioning.getAttrs(prov.invoke(req).getElement(AdminConstants.E_ALWAYSONCLUSTER)));
    }
}
