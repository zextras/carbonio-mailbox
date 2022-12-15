// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.UCServiceInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_UC_SERVICE_RESPONSE)
@XmlType(propOrder = {})
public class CreateUCServiceResponse {

    /**
     * @zm-api-field-description Information about the newly created uc service
     */
    @XmlElement(name=AdminConstants.E_UC_SERVICE)
    private UCServiceInfo ucService;

    public CreateUCServiceResponse() {
    }

    public void setUCService(UCServiceInfo ucService) {
        this.ucService = ucService;
    }

    public UCServiceInfo getUCService() {
        return ucService;
    }
}
