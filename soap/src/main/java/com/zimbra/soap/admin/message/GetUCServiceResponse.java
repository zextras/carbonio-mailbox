// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.UCServiceInfo;

@XmlRootElement(name=AdminConstants.E_GET_UC_SERVICE_RESPONSE)
public class GetUCServiceResponse {

    /**
     * @zm-api-field-description Information about ucservice
     */
    @XmlElement(name=AdminConstants.E_UC_SERVICE)
    private final UCServiceInfo ucService;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetUCServiceResponse() {
        this(null);
    }

    public GetUCServiceResponse(UCServiceInfo ucService) {
        this.ucService = ucService;
    }

    public UCServiceInfo getUCService() { return ucService; }
}
