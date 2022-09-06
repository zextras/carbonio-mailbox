// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CalendarResourceInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CREATE_CALENDAR_RESOURCE_RESPONSE)
public class CreateCalendarResourceResponse {

  /**
   * @zm-api-field-description Details of created resource
   */
  @XmlElement(name = AdminConstants.E_CALENDAR_RESOURCE, required = true)
  private final CalendarResourceInfo calResource;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CreateCalendarResourceResponse() {
    this((CalendarResourceInfo) null);
  }

  public CreateCalendarResourceResponse(CalendarResourceInfo calResource) {
    this.calResource = calResource;
  }

  public CalendarResourceInfo getCalResource() {
    return calResource;
  }
}
