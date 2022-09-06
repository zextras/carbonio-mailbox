// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class DocumentInfo extends CommonDocumentInfo {

  /**
   * @zm-api-field-tag lock-owner-account-id
   * @zm-api-field-description Lock owner account ID
   */
  @XmlAttribute(name = MailConstants.A_LOCKOWNER_ID /* loid */, required = false)
  private String lockOwnerId;

  /**
   * @zm-api-field-tag lock-owner-account-email
   * @zm-api-field-description Lock owner account email address
   */
  @XmlAttribute(name = MailConstants.A_LOCKOWNER_EMAIL /* loe */, required = false)
  private String lockOwnerEmail;

  /**
   * @zm-api-field-tag lock-timestamp
   * @zm-api-field-description Lock timestamp
   */
  @XmlAttribute(name = MailConstants.A_LOCKTIMESTAMP /* lt */, required = false)
  private String lockOwnerTimestamp;

  public DocumentInfo() {
    this((String) null);
  }

  public DocumentInfo(String id) {
    super(id);
  }

  public void setLockOwnerId(String lockOwnerId) {
    this.lockOwnerId = lockOwnerId;
  }

  public void setLockOwnerEmail(String lockOwnerEmail) {
    this.lockOwnerEmail = lockOwnerEmail;
  }

  public void setLockOwnerTimestamp(String lockOwnerTimestamp) {
    this.lockOwnerTimestamp = lockOwnerTimestamp;
  }

  public String getLockOwnerId() {
    return lockOwnerId;
  }

  public String getLockOwnerEmail() {
    return lockOwnerEmail;
  }

  public String getLockOwnerTimestamp() {
    return lockOwnerTimestamp;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("lockOwnerId", lockOwnerId)
        .add("lockOwnerEmail", lockOwnerEmail)
        .add("lockOwnerTimestamp", lockOwnerTimestamp);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
