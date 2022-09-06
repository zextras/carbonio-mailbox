// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AccountConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class ZmgDeviceSpec {

  /**
   * @zm-api-field-tag app-id
   * @zm-api-field-description App ID.
   */
  @XmlAttribute(name = AccountConstants.E_APP_ID /* app id */, required = true)
  private final String appId;

  /**
   * @zm-api-field-tag registration-id
   * @zm-api-field-description The registration id of the device for push notifications.
   */
  @XmlAttribute(name = AccountConstants.A_REGISTRATION_ID /* registration id */, required = true)
  private final String registrationId;

  /**
   * @zm-api-field-tag push-provider
   * @zm-api-field-description the provider for pushing notifications to the device
   */
  @XmlAttribute(name = AccountConstants.A_PUSH_PROVIDER /* push provider */, required = true)
  private final String pushProvider;

  /**
   * @zm-api-field-tag os-name
   * @zm-api-field-description osName is the name of the operating system installed on the device.
   *     Example - ios, android
   */
  @XmlAttribute(name = AccountConstants.A_OS_NAME /* OS name */, required = false)
  private String osName;

  /**
   * @zm-api-field-tag os-version
   * @zm-api-field-description The osVersion should be specified in the following formats - a)
   *     majorVersion.minorVersion.microVersion b) majorVersion.minorVersion Example - iOS having
   *     versions like 7.0, 8.0.3, 8.1 etc. Android has OS version like 2.0, 3.1, 4.4, 5.0 etc
   */
  @XmlAttribute(name = AccountConstants.A_OS_VERSION /* OS version */, required = false)
  private String osVersion;

  /**
   * @zm-api-field-tag max-payload-size
   * @zm-api-field-description maxPayloadSize is the maximum number of bytes allowed for the push
   *     notification payload Example - iOS 7.0 default maxPayloadSize is 256 bytes iOS 8.0 onwards
   *     default maxPayloadSize is 2048 bytes Android default maxPayloadSize is 4096 bytes In case,
   *     the maxPayloadSize is not specified the default payload size defined in the above examples
   *     will be used while sending push notifications
   */
  @XmlAttribute(name = AccountConstants.A_MAX_PAYLOAD_SIZE /* max payload size */, required = false)
  private Integer maxPayloadSize;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ZmgDeviceSpec() {
    this((String) null, (String) null, (String) null);
  }

  public ZmgDeviceSpec(String appId, String registrationId, String pushProvider) {
    this.appId = appId;
    this.registrationId = registrationId;
    this.pushProvider = pushProvider;
  }

  public void setOSName(String osName) {
    this.osName = osName;
  }

  public void setOSVersion(String osVersion) {
    this.osVersion = osVersion;
  }

  public void setMaxPayloadSize(int maxPayloadSize) {
    this.maxPayloadSize = maxPayloadSize;
  }

  public String getAppId() {
    return appId;
  }

  public String getRegistrationId() {
    return registrationId;
  }

  public String getPushProvider() {
    return pushProvider;
  }

  public String getOSName() {
    if (osName != null) {
      return osName;
    }
    return "";
  }

  public String getOSVersion() {
    if (osVersion != null) {
      return osVersion;
    }
    return "";
  }

  public int getMaxPayloadSize() {
    if (maxPayloadSize != null) {
      return maxPayloadSize;
    }
    // 0 is just a place holder to avoid null pointer exception
    return 0;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("appId", appId)
        .add("registrationId", registrationId)
        .add("pushProvider", pushProvider)
        .add("osName", osName)
        .add("osVersion", osVersion)
        .add("maxPayloadSize", maxPayloadSize);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
