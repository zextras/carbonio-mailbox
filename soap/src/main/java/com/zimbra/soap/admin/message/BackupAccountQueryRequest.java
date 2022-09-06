// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import com.zimbra.soap.admin.type.BackupAccountQuerySpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-network-edition
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Backup Account query <br>
 *     For each account &lt;backup> is listed from the most recent to earlier ones.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = BackupConstants.E_BACKUP_ACCOUNT_QUERY_REQUEST)
public class BackupAccountQueryRequest {

  /**
   * @zm-api-field-description Query
   */
  @XmlElement(name = BackupConstants.E_QUERY /* query */, required = true)
  private final BackupAccountQuerySpec query;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private BackupAccountQueryRequest() {
    this((BackupAccountQuerySpec) null);
  }

  public BackupAccountQueryRequest(BackupAccountQuerySpec query) {
    this.query = query;
  }

  public BackupAccountQuerySpec getQuery() {
    return query;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("query", query);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
