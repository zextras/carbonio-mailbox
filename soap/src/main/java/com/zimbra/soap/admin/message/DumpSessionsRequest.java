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
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Dump sessions
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_DUMP_SESSIONS_REQUEST)
public class DumpSessionsRequest {

    /**
     * @zm-api-field-description List Sessions flag
     */
    @XmlAttribute(name=AdminConstants.A_LIST_SESSIONS, required=false)
    private final ZmBoolean includeAccounts;

    /**
     * @zm-api-field-description Group by account flag
     */
    @XmlAttribute(name=AdminConstants.A_GROUP_BY_ACCOUNT, required=false)
    private final ZmBoolean groupByAccount;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DumpSessionsRequest() {
        this((Boolean) null, (Boolean) null);
    }

    public DumpSessionsRequest(Boolean includeAccounts,
                    Boolean groupByAccount) {
        this.includeAccounts = ZmBoolean.fromBool(includeAccounts);
        this.groupByAccount = ZmBoolean.fromBool(groupByAccount);
    }

    public Boolean getIncludeAccounts() { return ZmBoolean.toBool(includeAccounts); }
    public Boolean getGroupByAccount() { return ZmBoolean.toBool(groupByAccount); }
}
