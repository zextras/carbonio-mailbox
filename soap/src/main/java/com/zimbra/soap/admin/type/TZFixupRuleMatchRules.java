// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class TZFixupRuleMatchRules {

  /**
   * @zm-api-field-tag stdoff
   * @zm-api-field-description offset from UTC in standard time; local = UTC + offset
   */
  @XmlAttribute(name = AdminConstants.A_STDOFF /* stdoff */, required = true)
  private long stdOffset;

  /**
   * @zm-api-field-tag dayoff
   * @zm-api-field-description offset from UTC in daylight time; present only if DST is used
   */
  @XmlAttribute(name = AdminConstants.A_DAYOFF /* dayoff */, required = true)
  private long dstOffset;

  /**
   * @zm-api-field-description Standard match rule
   */
  @XmlElement(name = AdminConstants.E_STANDARD /* standard */, required = true)
  private TZFixupRuleMatchRule standard;

  /**
   * @zm-api-field-description Daylight saving match rule
   */
  @XmlElement(name = AdminConstants.E_DAYLIGHT /* daylight */, required = true)
  private TZFixupRuleMatchRule daylight;

  public TZFixupRuleMatchRules() {}

  public void setStdOffset(long stdOffset) {
    this.stdOffset = stdOffset;
  }

  public void setDstOffset(long dstOffset) {
    this.dstOffset = dstOffset;
  }

  public void setStandard(TZFixupRuleMatchRule standard) {
    this.standard = standard;
  }

  public void setDaylight(TZFixupRuleMatchRule daylight) {
    this.daylight = daylight;
  }

  public long getStdOffset() {
    return stdOffset;
  }

  public long getDstOffset() {
    return dstOffset;
  }

  public TZFixupRuleMatchRule getStandard() {
    return standard;
  }

  public TZFixupRuleMatchRule getDaylight() {
    return daylight;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("stdOffset", stdOffset)
        .add("dstOffset", dstOffset)
        .add("standard", standard)
        .add("daylight", daylight);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
