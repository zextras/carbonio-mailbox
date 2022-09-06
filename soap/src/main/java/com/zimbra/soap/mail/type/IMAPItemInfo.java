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
public class IMAPItemInfo {
  /**
   * @zm-api-field-tag msg-id
   * @zm-api-field-description Message ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  protected final int id;

  /**
   * @zm-api-field-tag imap-uid
   * @zm-api-field-description IMAP UID
   */
  @XmlAttribute(name = MailConstants.A_IMAP_UID /* i4uid */, required = true)
  protected final int imapUid;

  @SuppressWarnings("unused")
  IMAPItemInfo() {
    this(0, 0);
  }

  public IMAPItemInfo(int id, int imapUid) {
    this.id = id;
    this.imapUid = imapUid;
  }

  public int getId() {
    return id;
  }

  public int getImapUid() {
    return imapUid;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("id", id).add("i4uid", imapUid);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
