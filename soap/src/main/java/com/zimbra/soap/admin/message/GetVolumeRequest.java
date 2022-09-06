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

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Volume
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_VOLUME_REQUEST)
public final class GetVolumeRequest {

  /**
   * @zm-api-field-tag volume-id
   * @zm-api-field-description ID of volume
   */
  @XmlAttribute(name = AdminConstants.A_ID, required = true)
  private final short id;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetVolumeRequest() {
    this((short) -1);
  }

  public GetVolumeRequest(short id) {
    this.id = id;
  }

  public short getId() {
    return id;
  }
}
