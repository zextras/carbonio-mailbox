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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"timezones", "inviteComponents", "calendarReplies"})
public class InviteWithGroupInfo {

    /**
     * @zm-api-field-tag appt-or-task
     * @zm-api-field-description Invite type - <b>appt|task</b>
     */
    @XmlAttribute(name=MailConstants.A_CAL_ITEM_TYPE /* type */, required=true)
    private final String calItemType;

    /**
     * @zm-api-field-description Timezones
     */
    @XmlElement(name=MailConstants.E_CAL_TZ /* tz */, required=false)
    private List<CalTZInfo> timezones = Lists.newArrayList();

    /**
     * @zm-api-field-description Invite components
     */
    @XmlElement(name=MailConstants.E_INVITE_COMPONENT /* comp */, required=false)
    private List<InviteComponentWithGroupInfo> inviteComponents = Lists.newArrayList();

    /**
     * @zm-api-field-description Replies
     */
    @ZimbraJsonArrayForWrapper
    @XmlElementWrapper(name=MailConstants.E_CAL_REPLIES /* replies */, required=false)
    @XmlElement(name=MailConstants.E_CAL_REPLY /* reply */, required=false)
    private List<CalendarReply> calendarReplies = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private InviteWithGroupInfo() {
        this(null);
    }

    public InviteWithGroupInfo(String calItemType) {
        this.calItemType = calItemType;
    }

    public void setTimezones(Iterable <CalTZInfo> timezones) {
        this.timezones.clear();
        if (timezones != null) {
            Iterables.addAll(this.timezones,timezones);
        }
    }

    public void addTimezone(CalTZInfo timezone) {
        this.timezones.add(timezone);
    }

    public void setInviteComponents(
                Iterable <InviteComponentWithGroupInfo> inviteComponents) {
        this.inviteComponents.clear();
        if (inviteComponents != null) {
            Iterables.addAll(this.inviteComponents,inviteComponents);
        }
    }

    public void addInviteComponent(
                InviteComponentWithGroupInfo inviteComponent) {
        this.inviteComponents.add(inviteComponent);
    }

    public void setCalendarReplies(Iterable <CalendarReply> calendarReplies) {
        this.calendarReplies.clear();
        if (calendarReplies != null) {
            Iterables.addAll(this.calendarReplies,calendarReplies);
        }
    }

    public void addCalendarReply(CalendarReply calendarReply) {
        this.calendarReplies.add(calendarReply);
    }

    public String getCalItemType() { return calItemType; }
    public List<CalTZInfo> getTimezones() {
        return Collections.unmodifiableList(timezones);
    }
    public List<InviteComponentWithGroupInfo> getInviteComponents() {
        return Collections.unmodifiableList(inviteComponents);
    }
    public List<CalendarReply> getCalendarReplies() {
        return Collections.unmodifiableList(calendarReplies);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("calItemType", calItemType)
            .add("timezones", timezones)
            .add("inviteComponents", inviteComponents)
            .add("calendarReplies", calendarReplies);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}
