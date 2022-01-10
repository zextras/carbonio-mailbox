// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Version Check
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_VC_REQUEST)
public class VersionCheckRequest {

    /**
     * @zm-api-field-description Action.  Either <b>check</b> or <b>status</b>
     */
    @XmlAttribute(name=AdminConstants.E_ACTION /* action */, required=true)
    private final String action;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private VersionCheckRequest() {
        this((String) null);
    }

    public VersionCheckRequest(String action) {
        this.action = action;
    }

    public String getAction() { return action; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("action", action);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
