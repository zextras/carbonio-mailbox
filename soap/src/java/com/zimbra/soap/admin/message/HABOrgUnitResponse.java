// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;

/**
 * @author zimbra
 *
 */
/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description response for HabOrgUnit creation/rename/deletion response
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_HAB_ORG_UNIT_RESPONSE)
@XmlType(propOrder = {})
public class HABOrgUnitResponse {

    /**
     * @zm-api-field-description List of HabOrg units under a domain
     */
    @XmlElement(name=AdminConstants.E_HAB_ORG_UNIT_NAME, required=false)
    private List<String> habOrgList = new ArrayList<String>();
    
    public HABOrgUnitResponse() {
        
    }

    public HABOrgUnitResponse(List<String> habOrgList) {
        this.habOrgList = habOrgList;
    }
    
    public List<String> getHabOrgList() {
        return habOrgList;
    }

    
    public void setHabOrgList(List<String> habOrgList) {
        this.habOrgList = habOrgList;
    }
    
    
}

