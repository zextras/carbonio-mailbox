// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.BySecondRuleInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class BySecondRule implements BySecondRuleInterface {

    /**
     * @zm-api-field-tag second-list
     * @zm-api-field-description Comma separated list of seconds where second is a number between 0 and 59
     */
    @XmlAttribute(name=MailConstants.A_CAL_RULE_BYSECOND_SECLIST /* seclist */, required=true)
    private final String list;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private BySecondRule() {
        this((String) null);
    }

    public BySecondRule(String list) {
        this.list = list;
    }

    @Override
    public BySecondRuleInterface create(String list) {
        return new BySecondRule(list);
    }

    @Override
    public String getList() { return list; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("list", list)
            .toString();
    }
}
