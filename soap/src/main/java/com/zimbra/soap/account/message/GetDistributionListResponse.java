// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.DistributionListInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AccountConstants.E_GET_DISTRIBUTION_LIST_RESPONSE)
public class GetDistributionListResponse {

    /**
     * @zm-api-field-description Distribution list
     */
    @XmlElement(name=AccountConstants.E_DL, required=false)
    DistributionListInfo dl;

    public GetDistributionListResponse() {
        this((DistributionListInfo)null);
    }

    public GetDistributionListResponse(DistributionListInfo dl) {
        setDl(dl);
    }

    public void setDl(DistributionListInfo dl) { this.dl = dl; }

    public DistributionListInfo getDl() { return dl; }
}
