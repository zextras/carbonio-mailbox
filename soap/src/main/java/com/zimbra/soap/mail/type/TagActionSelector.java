// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class TagActionSelector extends ActionSelector {

    /**
     * @zm-api-field-description Retention policy
     */
    @XmlElement(name=MailConstants.E_RETENTION_POLICY, required=false)
    protected RetentionPolicy retentionPolicy;

    public TagActionSelector() {
        this(null, null);
    }

    public TagActionSelector(String ids, String operation) {
        super(ids, operation);
    }

    public void setRetentionPolicy(RetentionPolicy rp) { this.retentionPolicy = rp; }

    public RetentionPolicy getRetentionPolicy() { return retentionPolicy; }
}
