// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.MailboxBlobConsistency;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHECK_BLOB_CONSISTENCY_RESPONSE)
public class CheckBlobConsistencyResponse {

  /**
   * @zm-api-field-description Information for mailboxes
   */
  @XmlElement(name = AdminConstants.E_MAILBOX /* mbox */, required = false)
  private List<MailboxBlobConsistency> mailboxes = Lists.newArrayList();

  public CheckBlobConsistencyResponse() {}

  public void setMailboxes(Iterable<MailboxBlobConsistency> mailboxes) {
    this.mailboxes.clear();
    if (mailboxes != null) {
      Iterables.addAll(this.mailboxes, mailboxes);
    }
  }

  public void addMailboxe(MailboxBlobConsistency mailboxe) {
    this.mailboxes.add(mailboxe);
  }

  public List<MailboxBlobConsistency> getMailboxes() {
    return Collections.unmodifiableList(mailboxes);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("mailboxes", mailboxes);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
