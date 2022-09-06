// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.SyncConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class SyncStateInfo {
  /**
   * @zm-api-field-tag syncState
   * @zm-api-field-description SyncState
   */
  @XmlAttribute(name = SyncConstants.E_SYNCSTATE /* syncState */, required = true)
  private final String syncState;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SyncStateInfo() {
    this((String) null);
  }

  public SyncStateInfo(String syncState) {
    this.syncState = syncState;
  }

  public String getSyncState() {
    return syncState;
  }
}
