// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class XMPPComponentSelector {

    /**
     * @zm-api-field-tag xmpp-comp-selector-by
     * @zm-api-field-description Select the meaning of <b>{xmpp-comp-selector-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY, required=true)
    private final XMPPComponentBy by;

    /**
     * @zm-api-field-tag xmpp-comp-selector-key
     * @zm-api-field-description The key used to identify the XMPP component.
     * Meaning determined by <b>{xmpp-comp-selector-by}</b>
     */
    @XmlValue
    private final String value;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private XMPPComponentSelector() {
        this(null, null);
    }

    public XMPPComponentSelector(XMPPComponentBy by, String value) {
        this.by = by;
        this.value = value;
    }

    public XMPPComponentBy getBy() { return by; }
    public String getValue() { return value; }
}
