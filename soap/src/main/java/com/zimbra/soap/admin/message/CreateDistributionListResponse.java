// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DistributionListInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_DISTRIBUTION_LIST_RESPONSE)
public class CreateDistributionListResponse {

    /**
     * @zm-api-field-description Information about the newly created distribution list
     */
    @XmlElement(name=AdminConstants.E_DL, required=true)
    private final DistributionListInfo dl;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private CreateDistributionListResponse() {
        this(null);
    }

    public CreateDistributionListResponse(DistributionListInfo dl) {
        this.dl = dl;
    }

    public DistributionListInfo getDl() { return dl; }
}
