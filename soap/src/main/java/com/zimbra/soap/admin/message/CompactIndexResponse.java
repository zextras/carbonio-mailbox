// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_COMPACT_INDEX_RESPONSE)
public class CompactIndexResponse {
  /**
   * @zm-api-field-tag status
   * @zm-api-field-description Status - one of <b>started|running|idle</b>
   */
  @XmlAttribute(name = AdminConstants.A_STATUS, required = true)
  private final String status;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CompactIndexResponse() {
    this((String) null);
  }

  public CompactIndexResponse(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
