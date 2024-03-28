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
import com.zimbra.soap.admin.type.GetSessionsSortBy;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Sessions
 * <br />
 * <b>Access</b>: domain admin sufficient (though a domain admin can't specify "domains" as a type)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_SESSIONS_REQUEST)
public class GetSessionsRequest {

    // Valid values are Case insensitive Session.Type
    /**
     * @zm-api-field-tag
     * @zm-api-field-description Type - valid values soap|imap|admin
     */
    @XmlAttribute(name=AdminConstants.A_TYPE, required=true)
    private final String type;

    /**
     * @zm-api-field-tag sort-by
     * @zm-api-field-description SortBy
     */
    @XmlAttribute(name=AdminConstants.A_SORT_BY, required=false)
    private final GetSessionsSortBy sortBy;

    /**
     * @zm-api-field-description Offset - the starting offset (0, 25, etc)
     */
    @XmlAttribute(name=AdminConstants.A_OFFSET, required=false)
    private final Long offset;

    /**
     * @zm-api-field-description Limit - the number of sessions to return per page (0 is default and means all)
     */
    @XmlAttribute(name=AdminConstants.A_LIMIT, required=false)
    private final Long limit;

    /**
     * @zm-api-field-description Refresh. If <b>1 (true)</b>, ignore any cached results and start fresh.
     */
    @XmlAttribute(name=AdminConstants.A_REFRESH, required=false)
    private final ZmBoolean refresh;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetSessionsRequest() {
        this(null, null,
            null, null, null);
    }

    public GetSessionsRequest(String type, GetSessionsSortBy sortBy,
                    Long offset, Long limit, Boolean refresh) {
        this.type = type;
        this.sortBy = sortBy;
        this.offset = offset;
        this.limit = limit;
        this.refresh = ZmBoolean.fromBool(refresh);
    }

    public String getType() { return type; }
    public GetSessionsSortBy getSortBy() { return sortBy; }
    public Long getOffset() { return offset; }
    public Long getLimit() { return limit; }
    public Boolean getRefresh() { return ZmBoolean.toBool(refresh); }
}
