// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.EffectiveRightsTargetInfo;
import com.zimbra.soap.admin.type.GranteeInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_EFFECTIVE_RIGHTS_RESPONSE)
@XmlType(propOrder = {})
public class GetEffectiveRightsResponse {

    /**
     * @zm-api-field-description Information about grantee
     */
    @XmlElement(name=AdminConstants.E_GRANTEE, required=true)
    private final GranteeInfo grantee;

    /**
     * @zm-api-field-description Information about target
     */
    @XmlElement(name=AdminConstants.E_TARGET, required=true)
    private final EffectiveRightsTargetInfo target;


    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetEffectiveRightsResponse() {
        this(null, null);
    }

    public GetEffectiveRightsResponse(GranteeInfo grantee, EffectiveRightsTargetInfo target) {
        this.grantee = grantee;
        this.target = target;
    }

    public GranteeInfo getGrantee() { return grantee; }
    public EffectiveRightsTargetInfo getTarget() { return target; }
}
