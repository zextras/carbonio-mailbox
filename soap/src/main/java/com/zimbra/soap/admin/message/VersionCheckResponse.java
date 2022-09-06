// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.VersionCheckInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_VC_RESPONSE)
@XmlType(propOrder = {})
public class VersionCheckResponse {

  /**
   * @zm-api-field-description Version check information
   */
  @XmlElement(name = AdminConstants.E_VERSION_CHECK, required = false)
  private VersionCheckInfo versionCheck;

  public VersionCheckResponse() {}

  public void setVersionCheck(VersionCheckInfo versionCheck) {
    this.versionCheck = versionCheck;
  }

  public VersionCheckInfo getVersionCheck() {
    return versionCheck;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("versionCheck", versionCheck);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
