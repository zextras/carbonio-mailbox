// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class MailboxWithMailboxId {

  /**
   * @zm-api-field-tag mailbox-id
   * @zm-api-field-description Mailbox ID
   */
  @XmlAttribute(name = AdminConstants.A_MAILBOXID /* mbxid */, required = true)
  private final int mbxid;

  /**
   * @zm-api-field-tag account-id
   * @zm-api-field-description Account ID
   */
  @XmlAttribute(name = AdminConstants.A_ACCOUNTID /* id */, required = false)
  private String accountId;

  // DeleteMailbox doesn't set this
  /**
   * @zm-api-field-tag size-in-bytes
   * @zm-api-field-description Size in bytes
   */
  @XmlAttribute(name = AdminConstants.A_SIZE /* s */, required = false)
  private final Long size;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private MailboxWithMailboxId() {
    this(0, null, null);
  }

  public MailboxWithMailboxId(int mbxid, String accountId, Long size) {
    this.mbxid = mbxid;
    this.size = size;
    this.accountId = accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public int getMbxid() {
    return mbxid;
  }

  public Long getSize() {
    return size;
  }

  public String getAccountId() {
    return accountId;
  }
}
