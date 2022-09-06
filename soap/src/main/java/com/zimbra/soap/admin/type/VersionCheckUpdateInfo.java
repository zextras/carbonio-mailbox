// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class VersionCheckUpdateInfo {

  /**
   * @zm-api-field-tag type
   * @zm-api-field-description Type
   */
  @XmlAttribute(name = AdminConstants.A_UPDATE_TYPE /* type */, required = false)
  private String type;

  /**
   * @zm-api-field-tag critical
   * @zm-api-field-description Critical
   */
  @XmlAttribute(name = AdminConstants.A_CRITICAL /* critical */, required = false)
  private ZmBoolean critical;

  /**
   * @zm-api-field-tag updateURL
   * @zm-api-field-description updateURL
   */
  @XmlAttribute(name = AdminConstants.A_UPDATE_URL /* updateURL */, required = false)
  private String updateURL;

  /**
   * @zm-api-field-tag description
   * @zm-api-field-description Description
   */
  @XmlAttribute(name = AdminConstants.A_DESCRIPTION /* description */, required = false)
  private String description;

  /**
   * @zm-api-field-tag shortversion
   * @zm-api-field-description Short Version
   */
  @XmlAttribute(name = AdminConstants.A_SHORT_VERSION /* shortversion */, required = false)
  private String shortVersion;

  /**
   * @zm-api-field-tag release
   * @zm-api-field-description Release
   */
  @XmlAttribute(name = AdminConstants.A_RELEASE /* release */, required = false)
  private String release;

  /**
   * @zm-api-field-tag version
   * @zm-api-field-description Version
   */
  @XmlAttribute(name = AdminConstants.A_VERSION /* version */, required = false)
  private String version;

  /**
   * @zm-api-field-tag buildtype
   * @zm-api-field-description Build Type
   */
  @XmlAttribute(name = AdminConstants.A_BUILDTYPE /* buildtype */, required = false)
  private String buildType;

  /**
   * @zm-api-field-tag platform
   * @zm-api-field-description Platform
   */
  @XmlAttribute(name = AdminConstants.A_PLATFORM /* platform */, required = false)
  private String platform;

  public VersionCheckUpdateInfo() {}

  public void setType(String type) {
    this.type = type;
  }

  public void setCritical(Boolean critical) {
    this.critical = ZmBoolean.fromBool(critical);
  }

  public void setUpdateURL(String updateURL) {
    this.updateURL = updateURL;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setShortVersion(String shortVersion) {
    this.shortVersion = shortVersion;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setBuildType(String buildType) {
    this.buildType = buildType;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getType() {
    return type;
  }

  public Boolean getCritical() {
    return ZmBoolean.toBool(critical);
  }

  public String getUpdateURL() {
    return updateURL;
  }

  public String getDescription() {
    return description;
  }

  public String getShortVersion() {
    return shortVersion;
  }

  public String getRelease() {
    return release;
  }

  public String getVersion() {
    return version;
  }

  public String getBuildType() {
    return buildType;
  }

  public String getPlatform() {
    return platform;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("type", type)
        .add("critical", critical)
        .add("updateURL", updateURL)
        .add("description", description)
        .add("shortVersion", shortVersion)
        .add("release", release)
        .add("version", version)
        .add("buildType", buildType)
        .add("platform", platform);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
