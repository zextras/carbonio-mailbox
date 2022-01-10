// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DistributionListInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_GET_ALL_DISTRIBUTION_LISTS_RESPONSE)
public class GetAllDistributionListsResponse {

    /**
     * @zm-api-field-description Information on distribution lists
     */
    @XmlElement(name=AdminConstants.E_DL, required=false)
    private List <DistributionListInfo> dls = Lists.newArrayList();

    public GetAllDistributionListsResponse() {
        this((List <DistributionListInfo>)null);
    }

    public GetAllDistributionListsResponse(List <DistributionListInfo> dls) {
        setDls(dls);
    }

    public GetAllDistributionListsResponse setDls(Collection<DistributionListInfo> dls) {
        this.dls.clear();
        if (dls != null) {
            this.dls.addAll(dls);
        }
        return this;
    }

    public GetAllDistributionListsResponse addDl(DistributionListInfo dl) {
        dls.add(dl);
        return this;
    }

    public List<DistributionListInfo> getDls() {
        return Collections.unmodifiableList(dls);
    }
}
