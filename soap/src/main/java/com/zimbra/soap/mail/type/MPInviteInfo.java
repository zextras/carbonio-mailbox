// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonArrayForWrapper;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"timezones", "calendarReplies", "inviteComponents"})
@GraphQLType(name = GqlConstants.CLASS_MP_INVITE_INFORMATION, description = "MP invite information")
public class MPInviteInfo {

  /**
   * @zm-api-field-tag type-appt|task
   * @zm-api-field-description Calendar item type - <b>appt|task</b>
   */
  @XmlAttribute(name = MailConstants.A_CAL_ITEM_TYPE /* type */, required = true)
  @GraphQLQuery(
      name = GqlConstants.CALENDAR_ITEM_TYPE,
      description = "Calendar item type - appt|task")
  private final String calItemType;

  /**
   * @zm-api-field-description Timezones
   */
  @XmlElement(name = MailConstants.E_CAL_TZ /* tz */, required = false)
  private List<CalTZInfo> timezones = Lists.newArrayList();

  /**
   * @zm-api-field-description Replies
   */
  @ZimbraJsonArrayForWrapper
  @XmlElementWrapper(name = MailConstants.E_CAL_REPLIES /* replies */, required = false)
  @XmlElement(name = MailConstants.E_CAL_REPLY /* reply */, required = false)
  private List<CalendarReply> calendarReplies = Lists.newArrayList();

  /**
   * @zm-api-field-description Invite components
   */
  @XmlElement(name = MailConstants.E_INVITE_COMPONENT /* comp */, required = false)
  private List<InviteComponent> inviteComponents = Lists.newArrayList();

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private MPInviteInfo() {
    this((String) null);
  }

  public MPInviteInfo(
      @GraphQLNonNull @GraphQLInputField(name = GqlConstants.CALENDAR_ITEM_TYPE)
          String calItemType) {
    this.calItemType = calItemType;
  }

  @GraphQLInputField(name = GqlConstants.TIMEZONES, description = "Timezones")
  public void setTimezones(Iterable<CalTZInfo> timezones) {
    this.timezones.clear();
    if (timezones != null) {
      Iterables.addAll(this.timezones, timezones);
    }
  }

  @GraphQLIgnore
  public void addTimezone(CalTZInfo timezone) {
    this.timezones.add(timezone);
  }

  @GraphQLInputField(name = GqlConstants.CALENDAR_REPLIES, description = "Replies")
  public void setCalendarReplies(Iterable<CalendarReply> calendarReplies) {
    this.calendarReplies.clear();
    if (calendarReplies != null) {
      Iterables.addAll(this.calendarReplies, calendarReplies);
    }
  }

  @GraphQLIgnore
  public void addCalendarReply(CalendarReply calendarReply) {
    this.calendarReplies.add(calendarReply);
  }

  @GraphQLInputField(name = GqlConstants.INVITE_COMPONENTS, description = "Invite components")
  public void setInviteComponents(Iterable<InviteComponent> inviteComponents) {
    this.inviteComponents.clear();
    if (inviteComponents != null) {
      Iterables.addAll(this.inviteComponents, inviteComponents);
    }
  }

  @GraphQLIgnore
  public void addInviteComponent(InviteComponent inviteComponent) {
    this.inviteComponents.add(inviteComponent);
  }

  @GraphQLQuery(
      name = GqlConstants.CALENDAR_ITEM_TYPE,
      description = "Calendar item type - appt|task")
  public String getCalItemType() {
    return calItemType;
  }

  @GraphQLQuery(name = GqlConstants.TIMEZONES, description = "Timezones")
  public List<CalTZInfo> getTimezones() {
    return Collections.unmodifiableList(timezones);
  }

  @GraphQLQuery(name = GqlConstants.CALENDAR_REPLIES, description = "Replies")
  public List<CalendarReply> getCalendarReplies() {
    return Collections.unmodifiableList(calendarReplies);
  }

  @GraphQLQuery(name = GqlConstants.INVITE_COMPONENTS, description = "Invite components")
  public List<InviteComponent> getInviteComponents() {
    return Collections.unmodifiableList(inviteComponents);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("calItemType", calItemType)
        .add("timezones", timezones)
        .add("calendarReplies", calendarReplies)
        .add("inviteComponents", inviteComponents);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
