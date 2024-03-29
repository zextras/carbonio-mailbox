// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.ExceptionRuleInfoInterface;
import com.zimbra.soap.base.RecurrenceInfoInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class ExceptionRuleInfo
extends RecurIdInfo
implements RecurRuleBase, ExceptionRuleInfoInterface {

    /**
     * @zm-api-field-description Dates or rules which ADD instances.  ADDs are evaluated before EXCLUDEs
     */
    @XmlElement(name=MailConstants.E_CAL_ADD /* add */, required=false)
    private RecurrenceInfo add;

    /**
     * @zm-api-field-description Dates or rules which EXCLUDE instances
     */
    @XmlElement(name=MailConstants.E_CAL_EXCLUDE /* exclude */, required=false)
    private RecurrenceInfo exclude;

    public ExceptionRuleInfo() {
    }

    public void setAdd(RecurrenceInfo add) { this.add = add; }
    public void setExclude(RecurrenceInfo exclude) { this.exclude = exclude; }
    public RecurrenceInfo getAdd() { return add; }
    public RecurrenceInfo getExclude() { return exclude; }

    @Override
    public void setAddInterface(RecurrenceInfoInterface add) {
        setAdd((RecurrenceInfo) add);
    }

    @Override
    public void setExcludeInterface(RecurrenceInfoInterface exclude) {
        setExclude((RecurrenceInfo) exclude);
    }

    @Override
    public RecurrenceInfoInterface getAddInterface() {
        return add;
    }

    @Override
    public RecurrenceInfoInterface getExcludeInterface() {
        return exclude;
    }

    @Override
    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("add", add)
            .add("exclude", exclude);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
