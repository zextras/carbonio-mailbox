// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Purge the calendar cache for an account
 * <br />
 * <b>Access</b>: domain admin sufficient
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_PURGE_ACCOUNT_CALENDAR_CACHE_REQUEST)
public class PurgeAccountCalendarCacheRequest {

    /**
     * @zm-api-field-tag value-of-id
     * @zm-api-field-description Zimbra ID
     */
    @XmlAttribute(name=AdminConstants.A_ID, required=true)
    private final String id;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private PurgeAccountCalendarCacheRequest() {
        this((String) null);
    }

    public PurgeAccountCalendarCacheRequest(String id) {
        this.id = id;
    }

    public String getId() { return id; }
}
