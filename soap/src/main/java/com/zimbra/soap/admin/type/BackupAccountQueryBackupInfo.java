// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.BackupConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class BackupAccountQueryBackupInfo {

  /**
   * @zm-api-field-tag backup-label
   * @zm-api-field-description Backup label
   */
  @XmlAttribute(name = BackupConstants.A_LABEL /* label */, required = false)
  private String label;

  /**
   * @zm-api-field-tag backup-type
   * @zm-api-field-description Backup type - <b>full|incremental</b>
   */
  @XmlAttribute(name = BackupConstants.A_TYPE /* type */, required = false)
  private String type;

  /**
   * @zm-api-field-tag start-time-in-millis
   * @zm-api-field-description Start time in milliseconds
   */
  @XmlAttribute(name = BackupConstants.A_START /* start */, required = false)
  private Long start;

  /**
   * @zm-api-field-tag end-time-in-millis
   * @zm-api-field-description End time in milliseconds
   */
  @XmlAttribute(name = BackupConstants.A_END /* end */, required = false)
  private Long end;

  /**
   * @zm-api-field-tag account-uid
   * @zm-api-field-description Account UID
   */
  @XmlAttribute(name = BackupConstants.A_ACCOUNT_ID /* accountId */, required = false)
  private String accountId;

  public BackupAccountQueryBackupInfo() {}

  public void setLabel(String label) {
    this.label = label;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setStart(Long start) {
    this.start = start;
  }

  public void setEnd(Long end) {
    this.end = end;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getLabel() {
    return label;
  }

  public String getType() {
    return type;
  }

  public Long getStart() {
    return start;
  }

  public Long getEnd() {
    return end;
  }

  public String getAccountId() {
    return accountId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("label", label)
        .add("type", type)
        .add("start", start)
        .add("end", end)
        .add("accountId", accountId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
