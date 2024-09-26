// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Delete a new Calendar Group
 */
@XmlRootElement(name = MailConstants.E_DELETE_CALENDAR_GROUP_REQUEST)
public class DeleteCalendarGroupRequest {
  /** no-argument constructor wanted by JAXB */
  public DeleteCalendarGroupRequest() {}

  /**
   * @zm-api-field-tag id
   * @zm-api-field-description Calendar Group id
   */
  @XmlAttribute(name = "id" /* id */, required = true)
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
