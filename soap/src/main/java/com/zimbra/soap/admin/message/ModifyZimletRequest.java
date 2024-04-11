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
import com.zimbra.soap.admin.type.ZimletAclStatusPri;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify Zimlet
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MODIFY_ZIMLET_REQUEST)
public class ModifyZimletRequest {

    /**
     * @zm-api-field-description New Zimlet information
     */
    @XmlElement(name=AdminConstants.E_ZIMLET, required=true)
    private final ZimletAclStatusPri zimlet;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ModifyZimletRequest() {
        this(null);
    }

    public ModifyZimletRequest(ZimletAclStatusPri zimlet) {
        this.zimlet = zimlet;
    }

    public ZimletAclStatusPri getZimlet() { return zimlet; }
}
