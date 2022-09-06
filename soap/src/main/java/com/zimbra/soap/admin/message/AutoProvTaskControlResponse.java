// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = AdminConstants.E_AUTO_PROV_TASK_CONTROL_RESPONSE)
public class AutoProvTaskControlResponse {

  @XmlEnum
  public static enum Status {
    started,
    running,
    idle,
    stopped
  }

  /**
   * @zm-api-field-description Status - one of <b>started|running|idle|stopped</b>
   */
  @XmlAttribute(name = AdminConstants.A_STATUS, required = true)
  private final Status status;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private AutoProvTaskControlResponse() {
    this((Status) null);
  }

  public AutoProvTaskControlResponse(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }
}
