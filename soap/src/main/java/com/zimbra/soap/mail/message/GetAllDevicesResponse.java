// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.mail.type.DeviceInfo;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusXmlConstants.E_GET_ALL_DEVICES_RESPONSE)
public class GetAllDevicesResponse {

  /**
   * @zm-api-field-description Information for devices
   */
  @XmlElement(name = MailConstants.E_DEVICE /* device */, required = false)
  private List<DeviceInfo> devices = Lists.newArrayList();

  public GetAllDevicesResponse() {}

  public void setDevices(Iterable<DeviceInfo> devices) {
    this.devices.clear();
    if (devices != null) {
      Iterables.addAll(this.devices, devices);
    }
  }

  public void addDevice(DeviceInfo device) {
    this.devices.add(device);
  }

  public List<DeviceInfo> getDevices() {
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
