// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.GranteeInfo;
import com.zimbra.soap.admin.type.EffectiveRightsTarget;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ALL_EFFECTIVE_RIGHTS_RESPONSE)
@XmlType(propOrder = {"grantee", "targets"})
public class GetAllEffectiveRightsResponse {

    /**
     * @zm-api-field-description Grantee information
     */
    @XmlElement(name=AdminConstants.E_GRANTEE, required=false)
    private final GranteeInfo grantee;

    /**
     * @zm-api-field-description Effective rights targets
     */
    @XmlElement(name=AdminConstants.E_TARGET, required=false)
    private List <EffectiveRightsTarget> targets = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetAllEffectiveRightsResponse() {
        this((GranteeInfo) null);
    }

    public GetAllEffectiveRightsResponse(GranteeInfo grantee) {
        this.grantee = grantee;
    }

    public GetAllEffectiveRightsResponse setTargets(
            Collection <EffectiveRightsTarget> targets) {
        this.targets.clear();
        if (targets != null) {
            this.targets.addAll(targets);
        }
        return this;
    }

    public GetAllEffectiveRightsResponse addTarget(
            EffectiveRightsTarget target) {
        targets.add(target);
        return this;
    }

    public List <EffectiveRightsTarget> getTargets() {
        return Collections.unmodifiableList(targets);
    }

    public GranteeInfo getGrantee() { return grantee; }
}
