// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.Name;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Schedule backups
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = BackupConstants.E_SCHEDULE_BACKUPS_REQUEST)
public class ScheduleBackupsRequest {

  /**
   * @zm-api-field-description Server specification
   */
  @XmlElement(name = AdminConstants.E_SERVER /* server */, required = true)
  private final Name server;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ScheduleBackupsRequest() {
    this((Name) null);
  }

  public ScheduleBackupsRequest(Name server) {
    this.server = server;
  }

  public Name getServer() {
    return server;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("server", server);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
