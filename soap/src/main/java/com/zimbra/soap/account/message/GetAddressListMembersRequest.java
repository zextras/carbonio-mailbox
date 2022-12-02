package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.AttrsImpl;
import com.zimbra.soap.type.ZmBoolean;

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/**
* @zm-api-command-auth-required true
* @zm-api-command-admin-auth-required false
* @zm-api-command-description Get a distribution list, optionally with ownership information an granted rights.
* <br />
* Notes:
* <ul>
* <li> If the authed account is one of the list owners, all (requested) attributes of the DL are returned in the
*      response.  Otherwise only attributes visible and useful to non-owners are returned.
* <li> Specified &lt;rights> are returned only if the authed account is one of the list owners.
* <li> Only grants on this group entry are returned, inherited grants on domain or global grant are not returned.
* </ul>
*/

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_ADDRESS_LIST_MEMBERS_REQUEST)
public class GetAddressListMembersRequest {

    /**
     * @zm-api-field-description The number of members to return (0 is default
     *                           and means all)
     */
    @XmlAttribute(name = AccountConstants.A_LIMIT, required = false)
    private int limit;

    /**
     * @zm-api-field-description The starting offset (0, 25, etc)
     */
    @XmlAttribute(name = AccountConstants.A_OFFSET, required = false)
    private int offset;

    /**
     * @zm-api-field-description id of the distribution list
     */
    @XmlAttribute(name = AccountConstants.A_ID, required = true)
    private String id;

    /**
     * @zm-api-field-tag countOnly
     * @zm-api-field-description Set to 1 if the response should only contain
     *                           the count of total number of members. <br />
     *                           Set to 0 (default)
     */
    @XmlAttribute(name = AccountConstants.A_COUNT_ONLY, required = false)
    private ZmBoolean countOnly;

    public GetAddressListMembersRequest() {

    }

    public GetAddressListMembersRequest(String id) {
        this(id, 0, 0);
    }

    public GetAddressListMembersRequest(String id, int limit, int offset) {
        this.id = id;
        this.limit = limit;
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZmBoolean getCountOnly() {
        return countOnly;
    }

    public void setCountOnly(ZmBoolean countOnly) {
        this.countOnly = countOnly;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GetAddressListMembersRequest [limit=");
        builder.append(limit);
        builder.append(", offset=");
        builder.append(offset);
        builder.append(", ");
        if (id != null) {
            builder.append("id=");
            builder.append(id);
            builder.append(", ");
        }
        if (countOnly != null) {
            builder.append("countOnly=");
            builder.append(countOnly);
        }
        builder.append("]");
        return builder.toString();
    }

}
