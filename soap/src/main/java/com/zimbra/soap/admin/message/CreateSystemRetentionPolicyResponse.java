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
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.Policy;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_CREATE_SYSTEM_RETENTION_POLICY_RESPONSE)
public class CreateSystemRetentionPolicyResponse {

    /**
     * @zm-api-field-description Information about the newly created retention policy
     */
    @XmlElement(name=AdminConstants.E_POLICY, namespace=MailConstants.NAMESPACE_STR, required=true)
    private Policy policy;

    public CreateSystemRetentionPolicyResponse() {
    }

    public CreateSystemRetentionPolicyResponse(Policy p) {
        policy = p;
    }

    public Policy getPolicy() {
        return policy;
    }
}
