// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.MailboxByAccountIdSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Purges aged messages out of trash, spam, and entire mailbox <br>
 *     (if <b>&lt;mbox></b> element is omitted, purges all mailboxes on server)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_PURGE_MESSAGES_REQUEST)
public class PurgeMessagesRequest {

  /**
   * @zm-api-field-description Mailbox selector
   */
  @XmlElement(name = AdminConstants.E_MAILBOX, required = false)
  private final MailboxByAccountIdSelector mbox;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private PurgeMessagesRequest() {
    this((MailboxByAccountIdSelector) null);
  }

  public PurgeMessagesRequest(String accountId) {
    this(new MailboxByAccountIdSelector(accountId));
  }

  public PurgeMessagesRequest(MailboxByAccountIdSelector mbox) {
    this.mbox = mbox;
  }

  public MailboxByAccountIdSelector getMbox() {
    return mbox;
  }
}
