// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.RetentionPolicy;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_SYSTEM_RETENTION_POLICY_RESPONSE)
public class GetSystemRetentionPolicyResponse {

    /**
     * @zm-api-field-description System Retention policy
     */
    @XmlElement(name=MailConstants.E_RETENTION_POLICY)
    private RetentionPolicy retentionPolicy;

    /**
     * No-argument constructor for JAXB.
     */
    public GetSystemRetentionPolicyResponse() {
    }

    public GetSystemRetentionPolicyResponse(RetentionPolicy rp) {
        retentionPolicy = rp;
    }

    public RetentionPolicy getRetentionPolicy() {
        return retentionPolicy;
    }
}
