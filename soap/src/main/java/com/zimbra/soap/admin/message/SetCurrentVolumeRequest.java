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
 * @zm-api-command-description Set current volume. <br>
 *     Notes: Each SetCurrentVolumeRequest can set only one current volume type.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_SET_CURRENT_VOLUME_REQUEST)
public final class SetCurrentVolumeRequest {

  /**
   * @zm-api-field-description ID
   */
  @XmlAttribute(name = AdminConstants.A_ID, required = true)
  private final short id;

  /**
   * @zm-api-field-tag volume-type
   * @zm-api-field-description Volume type: 1 (primary message), 2 (secondary message) or 10 (index)
   */
  @XmlAttribute(name = AdminConstants.A_VOLUME_TYPE, required = true)
  private final short type;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SetCurrentVolumeRequest() {
    this((short) -1, (short) -1);
  }

  public SetCurrentVolumeRequest(short id, short type) {
    this.id = id;
    this.type = type;
  }

  public short getType() {
    return type;
  }

  public short getId() {
    return id;
  }
}
