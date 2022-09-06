// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class CalendarAttendeeWithGroupInfo extends CalendarAttendee {

  /**
   * @zm-api-field-tag is-group
   * @zm-api-field-description Set if the entry is a group
   */
  @XmlAttribute(name = MailConstants.A_IS_GROUP /* isGroup */, required = false)
  private ZmBoolean group;

  /**
   * @zm-api-field-tag can-expand-group-members
   * @zm-api-field-description Set if the user has the right to expand group members. Returned only
   *     if needExp is set in the request and only on group entries (isGroup is set).
   */
  @XmlAttribute(name = MailConstants.A_EXP /* exp */, required = false)
  private ZmBoolean canExpandGroupMembers;

  public CalendarAttendeeWithGroupInfo() {}

  public void setGroup(Boolean group) {
    this.group = ZmBoolean.fromBool(group);
  }

  public void setCanExpandGroupMembers(Boolean canExpandGroupMembers) {
    this.canExpandGroupMembers = ZmBoolean.fromBool(canExpandGroupMembers);
  }

  public Boolean getGroup() {
    return ZmBoolean.toBool(group);
  }

  public Boolean getCanExpandGroupMembers() {
    return ZmBoolean.toBool(canExpandGroupMembers);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper.add("group", group).add("canExpandGroupMembers", canExpandGroupMembers);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
