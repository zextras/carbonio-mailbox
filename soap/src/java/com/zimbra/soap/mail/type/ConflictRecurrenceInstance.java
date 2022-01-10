// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ConflictRecurrenceInstance extends ExpandedRecurrenceInstance {

    /**
     * @zm-api-field-description Free/Busy user status
     */
    @XmlElement(name=MailConstants.E_FREEBUSY_USER /* usr */, required=false)
    private List<FreeBusyUserStatus> freebusyUsers = Lists.newArrayList();

    public ConflictRecurrenceInstance() {
    }

    public void setFreebusyUsers(Iterable <FreeBusyUserStatus> freebusyUsers) {
        this.freebusyUsers.clear();
        if (freebusyUsers != null) {
            Iterables.addAll(this.freebusyUsers,freebusyUsers);
        }
    }

    public ConflictRecurrenceInstance addFreebusyUser(FreeBusyUserStatus freebusyUser) {
        this.freebusyUsers.add(freebusyUser);
        return this;
    }

    public List<FreeBusyUserStatus> getFreebusyUsers() {
        return Collections.unmodifiableList(freebusyUsers);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("freebusyUsers", freebusyUsers);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
