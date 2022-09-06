// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.BackupInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = BackupConstants.E_BACKUP_RESPONSE)
@XmlType(propOrder = {})
public class BackupResponse {

  /**
   * @zm-api-field-description Information about the backup
   */
  @XmlElement(name = BackupConstants.E_BACKUP /* backup */, required = true)
  private final BackupInfo backup;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private BackupResponse() {
    this((BackupInfo) null);
  }

  public BackupResponse(BackupInfo backup) {
    this.backup = backup;
  }

  public BackupInfo getBackup() {
    return backup;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("backup", backup);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
