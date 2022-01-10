// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ZimletAcl {

    /**
     * @zm-api-field-tag cos-name
     * @zm-api-field-description Name of Class Of Service (COS)
     */
    @XmlAttribute(name=AdminConstants.A_COS /* cos */, required=false)
    private final String cos;

    /**
     * @zm-api-field-tag acl-grant-or-deny
     * @zm-api-field-description <b>grant</b> or <b>deny</b>
     */
    @XmlAttribute(name=AdminConstants.A_ACL /* acl */, required=false)
    private final String acl;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ZimletAcl() {
        this((String) null, (String) null);
    }

    public ZimletAcl(String cos, String acl) {
        this.cos = cos;
        this.acl = acl;
    }

    public String getCos() { return cos; }
    public String getAcl() { return acl; }
}
