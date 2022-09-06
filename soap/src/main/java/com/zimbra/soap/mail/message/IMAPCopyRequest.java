// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Return the count of recent items in the specified folder
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_IMAP_COPY_REQUEST)
public class IMAPCopyRequest {
  /**
   * @zm-api-field-tag ids
   * @zm-api-field-description comma separated list of integer ids
   */
  @XmlAttribute(name = MailConstants.A_IDS /* ids */, required = true)
  private final String ids;

  /**
   * @zm-api-field-tag t
   * @zm-api-field-description mail item type. Valid values are case insensitive types from {@link
   *     com.zimbra.cs.mailbox.MailItem.Type} enum
   */
  @XmlAttribute(name = MailConstants.A_ITEM_TYPE /* t */, required = true)
  private final String t;

  /**
   * @zm-api-field-tag l
   * @zm-api-field-description target folder ID
   */
  @XmlAttribute(name = MailConstants.A_FOLDER /* l */, required = true)
  private final int folder;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private IMAPCopyRequest() {
    this(null, null, 0);
  }

  public IMAPCopyRequest(String ids, String type, int folder) {
    t = type;
    this.ids = ids;
    this.folder = folder;
  }

  public String getIds() {
    return ids;
  }

  public String getType() {
    return t;
  }

  public int getFolder() {
    return folder;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("ids", ids);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
