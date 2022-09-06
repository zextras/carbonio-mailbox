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
public class ActivityFilter {

  /**
   * @zm-api-field-tag account-id
   * @zm-api-field-description Account ID
   */
  @XmlAttribute(name = MailConstants.A_ACCOUNT /* account */, required = false)
  private String accountIds;

  // Values in list from enum com.zimbra.cs.mailbox.MailboxOperation which contains LOTS of
  // operations
  /**
   * @zm-api-field-tag comma-sep-ops
   * @zm-api-field-description Comma separated list of Mailbox operations
   */
  @XmlAttribute(name = MailConstants.A_OPERATION /* op */, required = false)
  private String ops;

  /**
   * @zm-api-field-tag session-id
   * @zm-api-field-description Session ID
   */
  @XmlAttribute(name = MailConstants.A_SESSION /* session */, required = false)
  private String sessionId;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ActivityFilter() {
    this((String) null, (String) null, (String) null);
  }

  public ActivityFilter(String accountIds, String ops, String sessionId) {
    this.accountIds = accountIds;
    this.ops = ops;
    this.sessionId = sessionId;
  }

  public void setAccount(String accountIds) {
    this.accountIds = accountIds;
  }

  public void setOp(String ops) {
    this.ops = ops;
  }

  public void setSession(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getAccount() {
    return accountIds;
  }

  public String getOp() {
    return ops;
  }

  public String getSession() {
    return sessionId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("account", accountIds).add("op", ops).add("session", sessionId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
