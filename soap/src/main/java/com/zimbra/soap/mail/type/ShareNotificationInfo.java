// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ShareNotificationInfo {

  /**
   * @zm-api-field-tag status-new|seen
   * @zm-api-field-description Status - "new" if the message is unread or "seen" if the message is
   *     read.
   */
  @XmlAttribute(name = MailConstants.A_STATUS /* status */, required = true)
  private final String status;

  /**
   * @zm-api-field-tag notification-item-id
   * @zm-api-field-description The item ID of the share notification message. The message must be in
   *     the Inbox folder.
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private final String id;

  /**
   * @zm-api-field-tag date
   * @zm-api-field-description Date
   */
  @XmlAttribute(name = MailConstants.A_DATE /* d */, required = true)
  private final long date;

  /**
   * @zm-api-field-description Grantor information
   */
  @ZimbraUniqueElement
  @XmlElement(name = MailConstants.E_GRANTOR /* grantor */, required = true)
  private final Grantor grantor;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ShareNotificationInfo() {
    this(null, null, -1L, null);
  }

  public ShareNotificationInfo(String status, String id, long date, Grantor grantor) {
    this.status = status;
    this.id = id;
    this.date = date;
    this.grantor = grantor;
  }

  public String getStatus() {
    return status;
  }

  public String getId() {
    return id;
  }

  public long getDate() {
    return date;
  }

  public Grantor getGrantor() {
    return grantor;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("status", status).add("id", id).add("date", date).add("grantor", grantor);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
