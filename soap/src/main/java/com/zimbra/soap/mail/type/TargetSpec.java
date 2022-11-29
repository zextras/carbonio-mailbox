// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.AccountBy;
import com.zimbra.soap.type.TargetType;

@XmlAccessorType(XmlAccessType.NONE)
public class TargetSpec {

    /**
     * @zm-api-field-tag target-type
     * @zm-api-field-description Target type
     */
    @XmlAttribute(name=MailConstants.A_TARGET_TYPE /* type */, required=true)
    private final TargetType targetType;

    /**
     * @zm-api-field-tag target-selector-by
     * @zm-api-field-description Select the meaning of <b>{target-selector-key}</b>
     */
    @XmlAttribute(name=MailConstants.A_TARGET_BY /* by */, required=true)
    private final AccountBy accountBy;

    /**
     * @zm-api-field-tag target-selector-key
     * @zm-api-field-description The key used to identify the target. Meaning determined by <b>{target-selector-by}</b>
     */
    @XmlValue
    private String value;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private TargetSpec() {
        this((TargetType) null, (AccountBy) null);
    }

    public TargetSpec(TargetType targetType, AccountBy accountBy) {
        this.targetType = targetType;
        this.accountBy = accountBy;
    }

    public void setValue(String value) { this.value = value; }
    public TargetType getTargetType() { return targetType; }
    public AccountBy getAccountBy() { return accountBy; }
    public String getValue() { return value; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("targetType", targetType)
            .add("accountBy", accountBy)
            .add("value", value);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
