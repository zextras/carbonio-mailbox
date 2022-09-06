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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_INDEX_STATS_REQUEST)
public class GetIndexStatsRequest {

  /**
   * @zm-api-field-description Mailbox
   */
  @XmlElement(name = AdminConstants.E_MAILBOX /* mbox */, required = true)
  private final MailboxByAccountIdSelector mbox;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetIndexStatsRequest() {
    this((MailboxByAccountIdSelector) null);
  }

  public GetIndexStatsRequest(MailboxByAccountIdSelector mbox) {
    this.mbox = mbox;
  }

  public MailboxByAccountIdSelector getMbox() {
    return mbox;
  }
}
