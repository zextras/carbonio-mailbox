// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.XMPPComponentInfo;

/**
 * @zm-api-response-description Note:
 * <br />
 * Attributes that are not allowed to be got by the authenticated admin will be returned as :
 * <pre>
 *     &lt;a n=&quot;{attr-name}&quot; pd=&quot;1&quot;/&gt;
 * </pre>
 * To allow an admin to get all attributes, grant the getXMPPComponent right
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_XMPPCOMPONENT_RESPONSE)
public class GetXMPPComponentResponse {

    /**
     * @zm-api-field-description XMPP Component Information
     */
    @XmlElement(name=AccountConstants.E_XMPP_COMPONENT, required=true)
    private final XMPPComponentInfo component;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetXMPPComponentResponse() {
        this(null);
    }

    public GetXMPPComponentResponse(XMPPComponentInfo component) {
        this.component = component;
    }

    public XMPPComponentInfo getComponent() { return component; }
}
