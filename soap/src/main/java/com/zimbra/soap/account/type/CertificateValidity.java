// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3
package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class CertificateValidity {

  /**
   * @zm-api-field-tag startDate
   * @zm-api-field-description The date from which certificate validity starts.Value is number of
   *     milliseconds since epoch.
   */
  @ZimbraJsonAttribute
  @XmlElement(name = SmimeConstants.E_START_DATE, required = false)
  private long startDate;

  /**
   * @zm-api-field-tag endDate
   * @zm-api-field-description The expiration date.Value is number of milliseconds since epoch.
   */
  @ZimbraJsonAttribute
  @XmlElement(name = SmimeConstants.E_END_DATE, required = false)
  private long endDate;

  public long getStartDate() {
    return startDate;
  }

  public void setStartDate(long startDate) {
    this.startDate = startDate;
  }

  public long getEndDate() {
    return endDate;
  }

  public void setEndDate(long endDate) {
    this.endDate = endDate;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("startDate", startDate).add("endDate", endDate);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
