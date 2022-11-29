// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify attributes for a server
 * <br />
 * Notes:
 * <ul>
 * <li>an empty attribute value removes the specified attr
 * <li>his request is by default proxied to the referenced server
 * </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MODIFY_SERVER_REQUEST)
@XmlType(propOrder = {})
public class ModifyServerRequest extends AdminAttrsImpl {

    /**
     * @zm-api-field-tag value-of-id
     * @zm-api-field-description Zimbra ID
     */
    @XmlAttribute(name=AdminConstants.E_ID, required=true)
    private String id;

    public ModifyServerRequest() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
