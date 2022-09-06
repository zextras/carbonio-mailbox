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
public class MailboxVersionInfo {

  /**
   * @zm-api-field-tag mailbox-id
   * @zm-api-field-description Mailbox ID
   */
  @XmlAttribute(name = BackupConstants.A_MAILBOXID /* mbxid */, required = true)
  private int mailboxId;

  /**
   * @zm-api-field-tag mailbox-major-version
   * @zm-api-field-description Major version of mailbox
   */
  @XmlAttribute(name = BackupConstants.A_MAJOR_VERSION /* majorVer */, required = true)
  private short majorVersion;

  /**
   * @zm-api-field-tag mailbox-minor-version
   * @zm-api-field-description Minor version of mailbox
   */
  @XmlAttribute(name = BackupConstants.A_MINOR_VERSION /* minorVer */, required = true)
  private short minorVersion;

  /**
   * @zm-api-field-tag db-schema-version
   * @zm-api-field-description Database schema version
   */
  @XmlAttribute(name = BackupConstants.A_DB_VERSION /* dbVer */, required = true)
  private int dbVersion;

  /**
   * @zm-api-field-tag search-index-version
   * @zm-api-field-description Search index version
   */
  @XmlAttribute(name = BackupConstants.A_INDEX_VERSION /* indexVer */, required = true)
  private int indexVersion;

  public MailboxVersionInfo() {}

  private MailboxVersionInfo(
      int mailboxId, short majorVersion, short minorVersion, int dbVersion, int indexVersion) {
    setMailboxId(mailboxId);
    setMajorVersion(majorVersion);
    setMinorVersion(minorVersion);
    setDbVersion(dbVersion);
    setIndexVersion(indexVersion);
  }

  public static MailboxVersionInfo createFromMailboxIdMajorMinorDbIndexVersions(
      int mailboxId, short majorVersion, short minorVersion, int dbVersion, int indexVersion) {
    return new MailboxVersionInfo(mailboxId, majorVersion, minorVersion, dbVersion, indexVersion);
  }

  public void setMailboxId(int mailboxId) {
    this.mailboxId = mailboxId;
  }

  public void setMajorVersion(short majorVersion) {
    this.majorVersion = majorVersion;
  }

  public void setMinorVersion(short minorVersion) {
    this.minorVersion = minorVersion;
  }

  public void setDbVersion(int dbVersion) {
    this.dbVersion = dbVersion;
  }

  public void setIndexVersion(int indexVersion) {
    this.indexVersion = indexVersion;
  }

  public int getMailboxId() {
    return mailboxId;
  }

  public short getMajorVersion() {
    return majorVersion;
  }

  public short getMinorVersion() {
    return minorVersion;
  }

  public int getDbVersion() {
    return dbVersion;
  }

  public int getIndexVersion() {
    return indexVersion;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("mailboxId", mailboxId)
        .add("majorVersion", majorVersion)
        .add("minorVersion", minorVersion)
        .add("dbVersion", dbVersion)
        .add("indexVersion", indexVersion);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
