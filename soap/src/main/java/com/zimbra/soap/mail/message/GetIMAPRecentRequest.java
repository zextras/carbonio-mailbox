// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Return the count of recent items in the specified folder
 */
@XmlRootElement(name = MailConstants.E_GET_IMAP_RECENT_REQUEST)
public class GetIMAPRecentRequest {

  /**
   * @zm-api-field-tag folder-id
   * @zm-api-field-description Folder ID
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private final String id;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetIMAPRecentRequest() {
    this((String) null);
  }

  public GetIMAPRecentRequest(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
