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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

/**
 * @author zimbra
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_MODIFY_HAB_GROUP_RESPONSE)
public class ModifyHABGroupResponse {

    /**
     * @zm-api-field-description List of HabOrggroups under the target parent group
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=AdminConstants.E_MEMBERS /* members */, required=false)
    @XmlElement(name = AdminConstants.E_MEMBER /*group child member*/, required = false)
    List<String> members = new ArrayList<String>();

    public ModifyHABGroupResponse() {

    }

    /**
     * @param members
     */
    public ModifyHABGroupResponse(List<String> members) {
        this.members = members;
    }

}
