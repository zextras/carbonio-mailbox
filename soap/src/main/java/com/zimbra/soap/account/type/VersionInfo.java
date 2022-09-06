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
public class VersionInfo {

  /**
   * @zm-api-field-tag version-string
   * @zm-api-field-description Full version string
   */
  @XmlAttribute(name = AccountConstants.A_VERSION_INFO_VERSION /* version */, required = true)
  private final String fullVersion;

  /**
   * @zm-api-field-tag release-string
   * @zm-api-field-description Release string
   */
  @XmlAttribute(name = AccountConstants.A_VERSION_INFO_RELEASE /* release */, required = true)
  private final String release;

  /**
   * @zm-api-field-tag build-date-YYYYMMDD-hhmm
   * @zm-api-field-description Build date in format: YYYYMMDD-hhmm
   */
  @XmlAttribute(name = AccountConstants.A_VERSION_INFO_DATE /* buildDate */, required = true)
  private final String date;

  /**
   * @zm-api-field-tag build-host-name
   * @zm-api-field-description Build host name
   */
  @XmlAttribute(name = AccountConstants.A_VERSION_INFO_HOST /* host */, required = true)
  private final String host;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private VersionInfo() {
    this((String) null, (String) null, (String) null, (String) null);
  }

  public VersionInfo(String fullVersion, String release, String date, String host) {
    this.fullVersion = fullVersion;
    this.release = release;
    this.date = date;
    this.host = host;
  }

  public String getFullVersion() {
    return fullVersion;
  }

  public String getRelease() {
    return release;
  }

  public String getDate() {
    return date;
  }

  public String getHost() {
    return host;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("fullVersion", fullVersion)
        .add("release", release)
        .add("date", date)
        .add("host", host)
        .toString();
  }
}
