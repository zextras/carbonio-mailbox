// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.UCServiceSelector;
import com.zimbra.soap.type.AttributeSelectorImpl;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get UC Service
 */
@XmlRootElement(name=AdminConstants.E_GET_UC_SERVICE_REQUEST)
public class GetUCServiceRequest extends AttributeSelectorImpl {

    /**
     * @zm-api-field-description UC Service
     */
    @XmlElement(name=AdminConstants.E_UC_SERVICE)
    private UCServiceSelector ucService;

    public GetUCServiceRequest() {
        this(null);
    }

    public GetUCServiceRequest(UCServiceSelector ucService) {
        setUCService(ucService);
    }

    public void setUCService(UCServiceSelector ucService) {
        this.ucService = ucService;
    }

    public UCServiceSelector getUCService() { return ucService; }
}

