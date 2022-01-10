package com.zimbra.soap.account.message;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.ZmBoolean;

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_ADDRESS_LIST_MEMBERS_RESPONSE)
public class GetAddressListMemberResponse {

    /**
     * @zm-api-field-tag more-flag
     * @zm-api-field-description 1 (true) if more members left to return
     */
    @XmlAttribute(name = AccountConstants.A_MORE /* more */, required = false)
    private ZmBoolean more;

    /**
     * @zm-api-field-tag total
     * @zm-api-field-description total number of distribution lists (not
     *                           affected by limit/offset)
     */
    @XmlAttribute(name = AccountConstants.A_TOTAL /* total */, required = true)
    private Integer total;

    /**
     * @zm-api-field-description Distribution list members
     */
    @XmlElement(name = AccountConstants.E_ADDRESS_LIST_MEMBERS /* alm */, required = false)
    private List<String> adlMembers = Lists.newArrayList();

    public GetAddressListMemberResponse() {

    }

    public ZmBoolean getMore() {
        return more;
    }

    public void setMore(ZmBoolean more) {
        this.more = more;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<String> getAdlMembers() {
        return adlMembers;
    }

    public void setAdlMembers(List<String> adlMembers) {
        this.adlMembers = adlMembers;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GetAddressListMemberResponse [");
        if (more != null) {
            builder.append("more=");
            builder.append(more);
            builder.append(", ");
        }
        if (total != null) {
            builder.append("total=");
            builder.append(total);
            builder.append(", ");
        }
        if (adlMembers != null) {
            builder.append("adlMembers=");
            builder.append(adlMembers);
        }
        builder.append("]");
        return builder.toString();
    }

    
}
