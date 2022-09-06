// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class CalendarResourceInfo extends AccountKeyValuePairs {

  /**
   * @zm-api-field-tag calendar-resource-name
   * @zm-api-field-description Name of calendar resource
   */
  @XmlAttribute(name = AccountConstants.A_NAME /* name */, required = true)
  private String name;

  /**
   * @zm-api-field-tag calendar-resource-id
   * @zm-api-field-description Name of calendar resource
   */
  @XmlAttribute(name = AccountConstants.A_ID /* id */, required = true)
  private String id;

  public CalendarResourceInfo() {}

  public void setName(String name) {
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    helper = super.addToStringInfo(helper);
    return helper.add("name", name).add("id", id);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
