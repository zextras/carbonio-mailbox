// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.sync.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.SyncConstants;
import com.zimbra.soap.sync.type.DeviceStatusInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = SyncConstants.E_REMOTE_WIPE_RESPONSE)
public class RemoteWipeResponse {

  /**
   * @zm-api-field-description Device status information
   */
  @XmlElement(name = SyncConstants.E_DEVICE /* device */, required = false)
  private DeviceStatusInfo device;

  public RemoteWipeResponse() {}

  public void setDevice(DeviceStatusInfo device) {
    this.device = device;
  }

  public DeviceStatusInfo getDevice() {
    return device;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("device", device);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
