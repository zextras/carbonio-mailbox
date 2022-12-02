// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.DistributionListSubscribeStatus;

@XmlRootElement(name=AccountConstants.E_SUBSCRIBE_DISTRIBUTION_LIST_RESPONSE)
public class SubscribeDistributionListResponse {

    /**
     * @zm-api-field-description Status of subscription attempt
     */
    @XmlAttribute(name=AccountConstants.A_STATUS, required=true)
    private DistributionListSubscribeStatus status;

    public SubscribeDistributionListResponse() {
        this((DistributionListSubscribeStatus) null);
    }
    public SubscribeDistributionListResponse(DistributionListSubscribeStatus status) {
        this.status = status;
    }

    public DistributionListSubscribeStatus getStatus() { return status; }
}
