// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.soap.admin.type.Attr;
import com.zimbra.soap.admin.type.DLInfo;
import com.zimbra.soap.admin.type.DistributionListInfo;
import com.zimbra.soap.admin.type.DistributionListMembershipInfo;

class SoapDistributionList extends DistributionList implements SoapEntry {

    SoapDistributionList(String name, String id, Map<String, Object> attrs, Provisioning prov) {
        super(name, id, attrs, prov);
    }

    /**
     * @param dlInfo contains information about a DL that another DL is
     *        a member of.
     * @throws ServiceException
     */
    SoapDistributionList(DistributionListMembershipInfo dlInfo,
            Provisioning prov)
    throws ServiceException {
        // DistributionListMembershipInfo does not supply attributes
        super(dlInfo.getName(), dlInfo.getId(),
                new HashMap<String,Object>(), prov);
        // DistributionListMembershipInfo does not supply membership info
        addDlm(new ArrayList<String>(), getRawAttrs());
    }

    SoapDistributionList(DistributionListInfo dlInfo, Provisioning prov)
    throws ServiceException {
        super(dlInfo.getName(), dlInfo.getId(), 
                Attr.collectionToMap(dlInfo.getAttrList()), prov);
        addDlm(dlInfo.getMembers(), getRawAttrs());
    }

    SoapDistributionList(DLInfo dlInfo, Provisioning prov)
    throws ServiceException {
        super(dlInfo.getName(), dlInfo.getId(), 
                Attr.collectionToMap(dlInfo.getAttrList()), prov);
        
        // DLInfo does not supply zimbraId
        Map<String, Object> attrs = getRawAttrs();
        attrs.put(Provisioning.A_zimbraId, dlInfo.getId());
        
        // DLInfo does not supply membership info
        addDlm(new ArrayList<String>(), getRawAttrs());
    }

    SoapDistributionList(Element e, Provisioning prov) throws ServiceException {
        super(e.getAttribute(AdminConstants.A_NAME), 
                e.getAttribute(AdminConstants.A_ID), 
                SoapProvisioning.getAttrs(e), prov);
        addDlm(e, getRawAttrs());
    }

    private void addDlm(List <String> members, Map<String, Object> attrs) {
        attrs.put(Provisioning.A_zimbraMailForwardingAddress,
                members.toArray(new String[members.size()]));
    }

    private void addDlm(Element e, Map<String, Object> attrs) {
        ArrayList<String> list = new ArrayList<String>();
        for (Element dlm : e.listElements(AdminConstants.E_DLM)) {
            list.add(dlm.getText());
        }
        addDlm(list, attrs);
    }

    public void modifyAttrs(SoapProvisioning prov, Map<String, ? extends Object> attrs, 
            boolean checkImmutable) throws ServiceException {
        XMLElement req = new XMLElement(AdminConstants.MODIFY_DISTRIBUTION_LIST_REQUEST);
        req.addElement(AdminConstants.E_ID).setText(getId());
        SoapProvisioning.addAttrElements(req, attrs);
        Element dl = prov.invoke(req).getElement(AdminConstants.E_DL);
        Map<String, Object> newAttrs = SoapProvisioning.getAttrs(dl);        
        addDlm(dl, newAttrs);
        setAttrs(newAttrs);        
    }

    public void reload(SoapProvisioning prov) throws ServiceException {
        XMLElement req = new XMLElement(AdminConstants.GET_DISTRIBUTION_LIST_REQUEST);
        Element a = req.addElement(AdminConstants.E_DL);
        a.setText(getId());
        a.addAttribute(AdminConstants.A_BY, Key.DistributionListBy.id.name());
        Element dl = prov.invoke(req).getElement(AdminConstants.E_DL);
        Map<String, Object> attrs = SoapProvisioning.getAttrs(dl);
        addDlm(dl, attrs);                
        setAttrs(attrs);
    }
}
