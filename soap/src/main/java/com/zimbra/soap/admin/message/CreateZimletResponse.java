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
import com.zimbra.soap.admin.type.ZimletInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_ZIMLET_RESPONSE)
public class CreateZimletResponse {

    /**
     * @zm-api-field-description Information about the newly created zimlet
     */
    @XmlElement(name=AdminConstants.E_ZIMLET, required=true)
    private final ZimletInfo zimlet;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CreateZimletResponse() {
        this(null);
    }

    public CreateZimletResponse(ZimletInfo zimlet) {
        this.zimlet = zimlet;
    }

    public ZimletInfo getZimlet() { return zimlet; }
}
