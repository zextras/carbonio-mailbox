// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.XMPPComponentInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ALL_XMPPCOMPONENTS_RESPONSE)
public class GetAllXMPPComponentsResponse {

    /**
     * @zm-api-field-description Information on XMPP components
     */
    @XmlElement(name=AccountConstants.E_XMPP_COMPONENT, required=false)
    private List<XMPPComponentInfo> components = Lists.newArrayList();

    public GetAllXMPPComponentsResponse() {
    }

    public void setComponents(Iterable <XMPPComponentInfo> components) {
        this.components.clear();
        if (components != null) {
            Iterables.addAll(this.components,components);
        }
    }

    public GetAllXMPPComponentsResponse addComponent(
                            XMPPComponentInfo component) {
        this.components.add(component);
        return this;
    }

    public List<XMPPComponentInfo> getComponents() {
        return Collections.unmodifiableList(components);
    }
}
