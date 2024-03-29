// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get the list of members of a distribution list.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_DISTRIBUTION_LIST_MEMBERS_REQUEST)
public class GetDistributionListMembersRequest {

    /**
     * @zm-api-field-description The number of members to return (0 is default and means all)
     */
    @XmlAttribute(name=AdminConstants.A_LIMIT, required=false)
    private final Integer limit;

    /**
     * @zm-api-field-description The starting offset (0, 25, etc)
     */
    @XmlAttribute(name=AdminConstants.A_OFFSET, required=false)
    private final Integer offset;

    /**
     * @zm-api-field-tag dl-name
     * @zm-api-field-description The name of the distribution list
     */
    @XmlElement(name=AdminConstants.E_DL, required=true)
    private final String dl;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetDistributionListMembersRequest() {
        this(null, null, null);
    }

    public GetDistributionListMembersRequest(Integer limit, Integer offset,
                            String dl) {
        this.limit = limit;
        this.offset = offset;
        this.dl = dl;
    }

    public Integer getLimit() { return limit; }
    public Integer getOffset() { return offset; }
    public String getDl() { return dl; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("limit", limit)
            .add("offset", offset)
            .add("dl", dl)
            .toString();
    }
}
