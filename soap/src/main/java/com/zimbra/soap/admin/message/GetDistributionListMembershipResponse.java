// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DistributionListMembershipInfo;

/**
 * Response which provides a list of DLs that a particular DL is a member of
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_DISTRIBUTION_LIST_MEMBERSHIP_RESPONSE)
public class GetDistributionListMembershipResponse {

    /**
     * @zm-api-field-description Information about distribution lists
     */
    @XmlElement(name=AdminConstants.E_DL, required=false)
    private List <DistributionListMembershipInfo> dls = Lists.newArrayList();

    public GetDistributionListMembershipResponse() {
        this(null);
    }

    public GetDistributionListMembershipResponse(
            Iterable<DistributionListMembershipInfo> dls) {
        setDls(dls);
    }

    public GetDistributionListMembershipResponse setDls(
            Iterable<DistributionListMembershipInfo> dls) {
        this.dls.clear();
        if (dls != null) {
            Iterables.addAll(this.dls,dls);
        }
        return this;
    }

    public GetDistributionListMembershipResponse addDl(
            DistributionListMembershipInfo dl) {
        dls.add(dl);
        return this;
    }

    public List<DistributionListMembershipInfo> getDls() {
        return Collections.unmodifiableList(dls);
    }
}
