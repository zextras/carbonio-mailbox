// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.MailboxWithMailboxId;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_PURGE_MESSAGES_RESPONSE)
public class PurgeMessagesResponse {

  /**
   * @zm-api-field-description Information about mailboxes where aged messages have been purged
   */
  @XmlElement(name = AdminConstants.E_MAILBOX, required = false)
  private List<MailboxWithMailboxId> mailboxes = Lists.newArrayList();

  public PurgeMessagesResponse() {}

  public PurgeMessagesResponse setMailboxes(Collection<MailboxWithMailboxId> mailboxes) {
    this.mailboxes.clear();
    if (mailboxes != null) {
      this.mailboxes.addAll(mailboxes);
    }
    return this;
  }

  public PurgeMessagesResponse addMailbox(MailboxWithMailboxId attr) {
    mailboxes.add(attr);
    return this;
  }

  public List<MailboxWithMailboxId> getMailboxes() {
    return Collections.unmodifiableList(mailboxes);
  }
}
