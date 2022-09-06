// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class UpdatedAlarmInfo {

  /**
   * @zm-api-field-tag cal-item-id
   * @zm-api-field-description Calendar item ID
   */
  @XmlAttribute(name = MailConstants.A_CAL_ID /* calItemId */, required = true)
  private final String calItemId;

  /**
   * @zm-api-field-description Updated alarm information
   */
  @XmlElement(name = MailConstants.E_CAL_ALARM_DATA /* alarmData */, required = false)
  private AlarmDataInfo alarmData;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  protected UpdatedAlarmInfo() {
    this((String) null);
  }

  public UpdatedAlarmInfo(String calItemId) {
    this.calItemId = calItemId;
  }

  public void setAlarmData(AlarmDataInfo alarmData) {
    this.alarmData = alarmData;
  }

  public String getCalItemId() {
    return calItemId;
  }

  public AlarmDataInfo getAlarmData() {
    return alarmData;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("calItemId", calItemId).add("alarmData", alarmData);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
