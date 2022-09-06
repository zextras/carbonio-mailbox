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

@XmlAccessorType(XmlAccessType.NONE)
public class InstanceDataAttrs extends CommonInstanceDataAttrs {

  /**
   * @zm-api-field-tag duration
   * @zm-api-field-description Duration
   */
  @XmlAttribute(name = MailConstants.A_CAL_NEW_DURATION /* dur */, required = false)
  private Long duration;

  public InstanceDataAttrs() {}

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public Long getDuration() {
    return duration;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper.add("duration", duration);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
