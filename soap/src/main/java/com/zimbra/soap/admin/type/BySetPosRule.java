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
import com.zimbra.soap.base.BySetPosRuleInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class BySetPosRule implements BySetPosRuleInterface {

    /**
     * @zm-api-field-tag bysetpos-list
     * @zm-api-field-description Format <b>[[+]|-]num[,...]</b> where num is from 1 to 366
     * <br />
     * <b>&lt;bysetpos></b> MUST only be used in conjunction with another <b>&lt;byXXX></b> element.
     */
    @XmlAttribute(name=MailConstants.A_CAL_RULE_BYSETPOS_POSLIST /* poslist */, required=true)
    private final String list;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private BySetPosRule() {
        this((String) null);
    }

    public BySetPosRule(String list) {
        this.list = list;
    }

    @Override
    public BySetPosRuleInterface create(String list) {
        return new BySetPosRule(list);
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
