// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DistributionListInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_MODIFY_DISTRIBUTION_LIST_RESPONSE)
public class ModifyDistributionListResponse {

    /**
     * @zm-api-field-description Information about distribution list
     */
    @XmlElement(name=AdminConstants.E_DL, required=false)
    DistributionListInfo dl;

    public ModifyDistributionListResponse() {
        this(null);
    }

    public ModifyDistributionListResponse(DistributionListInfo dl) {
        setDl(dl);
    }

    public void setDl(DistributionListInfo dl) { this.dl = dl; }

    public DistributionListInfo getDl() { return dl; }
}
