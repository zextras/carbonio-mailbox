// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.OctopusXmlConstants;
import com.zimbra.soap.mail.type.IdStatus;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Update device status
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = OctopusXmlConstants.E_UPDATE_DEVICE_STATUS_REQUEST)
public class UpdateDeviceStatusRequest {

  /**
   * @zm-api-field-description Information about device status. Setting of "status" attribute:
   *     <table>
   * <tr> <td> <b>enabled</b> </td> <td> in normal operation </td> </tr>
   * <tr> <td> <b>disabled</b> </td>
   *      <td> user or admin requested to disable this device.  the device will perform self wipe next time it
   *           contacts the server.  </td> </tr>
   * <tr> <td> <b>locked</b> </td> <td> device is temporarily locked </td> </tr>
   * <tr> <td> <b>wiped</b> </td>
   *      <td> device has acknowledged the disable request, and wiped the the downloaded files and
   *           authentication information.
   * </td> </tr>
   * </table>
   */
  @XmlElement(name = MailConstants.E_DEVICE /* device */, required = true)
  private final IdStatus device;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private UpdateDeviceStatusRequest() {
    this((IdStatus) null);
  }

  public UpdateDeviceStatusRequest(IdStatus device) {
    this.device = device;
  }

  public IdStatus getDevice() {
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
