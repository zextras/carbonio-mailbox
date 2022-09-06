// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.sync.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.SyncConstants;
import com.zimbra.soap.sync.type.DeviceStatusInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = SyncConstants.E_GET_DEVICE_STATUS_RESPONSE)
public class GetDeviceStatusResponse {

  /**
   * @zm-api-field-description Device status information
   */
  @XmlElement(name = SyncConstants.E_DEVICE /* device */, required = false)
  private List<DeviceStatusInfo> devices = Lists.newArrayList();

  public GetDeviceStatusResponse() {}

  public void setDevices(Iterable<DeviceStatusInfo> devices) {
    this.devices.clear();
    if (devices != null) {
      Iterables.addAll(this.devices, devices);
    }
  }

  public void addDevice(DeviceStatusInfo device) {
    this.devices.add(device);
  }

  public List<DeviceStatusInfo> getDevices() {
    return Collections.unmodifiableList(devices);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("devices", devices);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
