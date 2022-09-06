// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = AdminConstants.E_COUNT_OBJECTS_RESPONSE)
public class CountObjectsResponse {

  /**
   * @zm-api-field-tag num-objects
   * @zm-api-field-description Number of objects of the requested type
   */
  @XmlAttribute(name = AdminConstants.A_NUM, required = true)
  private long num;

  @XmlAttribute(name = AdminConstants.A_TYPE, required = true)
  private String type;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  public CountObjectsResponse() {
    this(0, "");
  }

  public CountObjectsResponse(long num, String type) {
    this.num = num;
    this.type = type;
  }

  public long getNum() {
    return num;
  }

  public String getType() {
    return type;
  }
}
