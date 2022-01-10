// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.EffectiveAttrsInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_CREATE_OBJECT_ATTRS_RESPONSE)
public class GetCreateObjectAttrsResponse {

    /**
     * @zm-api-field-description Set attributes
     */
    @XmlElement(name=AdminConstants.E_SET_ATTRS, required=true)
    private final EffectiveAttrsInfo setAttrs;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetCreateObjectAttrsResponse() {
        this((EffectiveAttrsInfo) null);
    }

    public GetCreateObjectAttrsResponse(EffectiveAttrsInfo setAttrs) {
        this.setAttrs = setAttrs;
    }

    public EffectiveAttrsInfo getSetAttrs() { return setAttrs; }
}
