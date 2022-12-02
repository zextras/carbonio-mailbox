// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface RecurrenceInfoInterface {
    public void setRuleInterfaces(Iterable<RecurRuleBaseInterface> rules);
    public void addRuleInterface(RecurRuleBaseInterface rule);
    public List<RecurRuleBaseInterface> getRuleInterfaces();
}
