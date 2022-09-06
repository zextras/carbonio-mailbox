// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.replication.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.ReplicationConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ReplicationMasterStatus {

  /**
   * @zm-api-field-tag master-operating-mode-invalid|normal|slaveless|catchup
   * @zm-api-field-description Master's operating mode - <b>invalid|normal|slaveless|catchup</b>
   */
  @XmlAttribute(
      name = ReplicationConstants.A_MASTER_OPERATING_MODE /* masterOperatingMode */,
      required = true)
  private final String masterOperatingMode;

  /**
   * @zm-api-field-description Catchup status
   */
  @ZimbraUniqueElement
  @XmlElement(name = ReplicationConstants.E_CATCHUP_STATUS /* catchupStatus */, required = false)
  private ReplicationMasterCatchupStatus catchupStatus;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ReplicationMasterStatus() {
    this((String) null);
  }

  public ReplicationMasterStatus(String masterOperatingMode) {
    this.masterOperatingMode = masterOperatingMode;
  }

  public void setCatchupStatus(ReplicationMasterCatchupStatus catchupStatus) {
    this.catchupStatus = catchupStatus;
  }

  public String getMasterOperatingMode() {
    return masterOperatingMode;
  }

  public ReplicationMasterCatchupStatus getCatchupStatus() {
    return catchupStatus;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("masterOperatingMode", masterOperatingMode)
        .add("catchupStatus", catchupStatus);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
