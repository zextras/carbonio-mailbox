// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.MailboxWithMailboxId;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = AdminConstants.E_GET_MAILBOX_RESPONSE)
public class GetMailboxResponse {

  /**
   * @zm-api-field-description Information about mailbox
   */
  @XmlElement(name = AdminConstants.E_MAILBOX, required = true)
  private final MailboxWithMailboxId mbox;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetMailboxResponse() {
    this(null);
  }

  public GetMailboxResponse(MailboxWithMailboxId mbox) {
    this.mbox = mbox;
  }

  public MailboxWithMailboxId getMbox() {
    return mbox;
  }
}
