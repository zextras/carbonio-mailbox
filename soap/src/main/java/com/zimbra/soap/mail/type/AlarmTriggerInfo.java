// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.gql.GqlConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.AlarmTriggerInfoInterface;
import com.zimbra.soap.base.DateAttrInterface;
import com.zimbra.soap.base.DurationInfoInterface;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
@GraphQLType(
    name = GqlConstants.CLASS_ALARM_TRIGGER_INFORMATION,
    description = "Alarm trigger information")
public class AlarmTriggerInfo implements AlarmTriggerInfoInterface {

  /**
   * @zm-api-field-description Absolute trigger information
   */
  @XmlElement(name = MailConstants.E_CAL_ALARM_ABSOLUTE /* abs */, required = false)
  private DateAttr absolute;

  /**
   * @zm-api-field-description Relative trigger information
   */
  @XmlElement(name = MailConstants.E_CAL_ALARM_RELATIVE /* rel */, required = false)
  private DurationInfo relative;

  public AlarmTriggerInfo() {}

  @GraphQLInputField(name = GqlConstants.ABSOLUTE, description = "Absolute trigger information")
  public void setAbsolute(DateAttr absolute) {
    this.absolute = absolute;
  }

  @GraphQLInputField(name = GqlConstants.RELATIVE, description = "Relative trigger information")
  public void setRelative(DurationInfo relative) {
    this.relative = relative;
  }

  @GraphQLQuery(name = GqlConstants.ABSOLUTE, description = "Absolute trigger information")
  public DateAttr getAbsolute() {
    return absolute;
  }

  @GraphQLQuery(name = GqlConstants.RELATIVE, description = "Relative trigger information")
  public DurationInfo getRelative() {
    return relative;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("absolute", absolute)
        .add("relative", relative)
        .toString();
  }

  @Override
  @GraphQLIgnore
  public void setAbsoluteInterface(DateAttrInterface absolute) {
    setAbsolute((DateAttr) absolute);
  }

  @Override
  @GraphQLIgnore
  public void setRelativeInterface(DurationInfoInterface relative) {
    setRelative((DurationInfo) relative);
  }

  @Override
  @GraphQLIgnore
  public DateAttrInterface getAbsoluteInterface() {
    return absolute;
  }

  @Override
  @GraphQLIgnore
  public DurationInfoInterface getRelativeInterface() {
    return relative;
  }
}
