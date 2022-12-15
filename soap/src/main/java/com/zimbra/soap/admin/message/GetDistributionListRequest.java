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

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DistributionListSelector;
import com.zimbra.soap.type.AttributeSelectorImpl;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get a Distribution List
 * <br />
 * <b>Access</b>: domain admin sufficient
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_DISTRIBUTION_LIST_REQUEST)
public class GetDistributionListRequest extends AttributeSelectorImpl {

    /**
     * @zm-api-field-tag limit
     * @zm-api-field-description The maximum number of accounts to return (0 is default and means all)
     */
    @XmlAttribute(name=AdminConstants.A_LIMIT, required=false)
    private Integer limit;

    /**
     * @zm-api-field-tag starting-offset
     * @zm-api-field-description The starting offset (0, 25 etc)
     */
    @XmlAttribute(name=AdminConstants.A_OFFSET, required=false)
    private Integer offset;

    /**
     * @zm-api-field-tag sort-ascending
     * @zm-api-field-description Flag whether to sort in ascending order <b>1 (true)</b> is the default
     */
    @XmlAttribute(name=AdminConstants.A_SORT_ASCENDING, required=false)
    private ZmBoolean sortAscending;

    /**
     * @zm-api-field-description Distribution List
     */
    @XmlElement(name=AdminConstants.E_DL, required=false)
    private DistributionListSelector dl;

    public GetDistributionListRequest() {
        this((DistributionListSelector) null,
            (Integer) null, (Integer) null, (Boolean) null);
    }

    public GetDistributionListRequest(DistributionListSelector dl) {
        this(dl, (Integer) null, (Integer) null, (Boolean) null);
    }

    public GetDistributionListRequest(DistributionListSelector dl,
            Integer limit, Integer offset, Boolean sortAscending) {
        setDl(dl);
        setLimit(limit);
        setOffset(offset);
        setSortAscending(sortAscending);
    }

    public void setDl(DistributionListSelector dl) { this.dl = dl; }
    public void setLimit(Integer limit) { this.limit = limit; }
    public void setOffset(Integer offset) { this.offset = offset; }
    public void setSortAscending(Boolean sortAscending) {
        this.sortAscending = ZmBoolean.fromBool(sortAscending);
    }

    public DistributionListSelector getDl() { return dl; }
    public Integer getLimit() { return limit; }
    public Integer getOffset() { return offset; }
    public Boolean isSortAscending() { return ZmBoolean.toBool(sortAscending); }
}
