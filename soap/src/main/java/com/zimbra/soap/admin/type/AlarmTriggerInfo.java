// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.AlarmTriggerInfoInterface;
import com.zimbra.soap.base.DateAttrInterface;
import com.zimbra.soap.base.DurationInfoInterface;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
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

  public void setAbsolute(DateAttr absolute) {
    this.absolute = absolute;
  }

  public void setRelative(DurationInfo relative) {
    this.relative = relative;
  }

  public DateAttr getAbsolute() {
    return absolute;
  }

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
  public void setAbsoluteInterface(DateAttrInterface absolute) {
    setAbsolute((DateAttr) absolute);
  }

  @Override
  public void setRelativeInterface(DurationInfoInterface relative) {
    setRelative((DurationInfo) relative);
  }

  @Override
  public DateAttrInterface getAbsoluteInterface() {
    return absolute;
  }

  @Override
  public DurationInfoInterface getRelativeInterface() {
    return relative;
  }
}
