// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.DateAttrInterface;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class DateAttr implements DateAttrInterface {

  /**
   * @zm-api-field-tag YYYYMMDDThhmmssZ
   * @zm-api-field-description Date in format : <b>YYYYMMDDThhmmssZ</b>
   */
  @XmlAttribute(name = MailConstants.A_DATE, required = true)
  private final String date;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DateAttr() {
    this((String) null);
  }

  public DateAttr(String date) {
    this.date = date;
  }

  @Override
  public DateAttrInterface create(String date) {
    return new DateAttr(date);
  }

  @Override
  public String getDate() {
    return date;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("date", date).toString();
  }
}
