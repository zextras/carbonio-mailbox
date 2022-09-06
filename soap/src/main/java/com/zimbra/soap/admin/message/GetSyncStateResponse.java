// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.SyncAdminConstants;
import com.zimbra.soap.admin.type.SyncStateInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = SyncAdminConstants.E_GET_SYNC_STATE_RESPONSE)
@XmlType(propOrder = {})
public class GetSyncStateResponse {

  /**
   * @zm-api-field-description Information about SyncState
   */
  @XmlElement(name = AdminConstants.E_DOMAIN, required = true)
  private SyncStateInfo syncState;

  public GetSyncStateResponse() {}

  public void setSyncState(SyncStateInfo syncState) {
    this.syncState = syncState;
  }

  public SyncStateInfo getSyncState() {
    return this.syncState;
  }
}
