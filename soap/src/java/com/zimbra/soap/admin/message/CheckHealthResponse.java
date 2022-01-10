// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CHECK_HEALTH_RESPONSE)
@XmlType(propOrder = {})
public class CheckHealthResponse {

    /**
     * @zm-api-field-description Flags whether healthy or not
     */
    @XmlAttribute(name=AdminConstants.A_HEALTHY, required=true)
    private ZmBoolean healthy;

    public CheckHealthResponse() { }

    public CheckHealthResponse(boolean healthy) { this.healthy = ZmBoolean.fromBool(healthy); }

    public void setHealthy(boolean healthy) { this.healthy = ZmBoolean.fromBool(healthy); }

    public boolean isHealthy() { return ZmBoolean.toBool(healthy); }
}
