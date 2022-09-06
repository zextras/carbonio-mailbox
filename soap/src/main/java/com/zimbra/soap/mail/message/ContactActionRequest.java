// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import com.zimbra.soap.mail.type.ContactActionSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Contact Action
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_CONTACT_ACTION_REQUEST)
public class ContactActionRequest {

  /**
   * @zm-api-field-description Contact action selector
   */
  @ZimbraUniqueElement
  @XmlElement(name = MailConstants.E_ACTION /* action */, required = true)
  private final ContactActionSelector action;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ContactActionRequest() {
    this((ContactActionSelector) null);
  }

  public ContactActionRequest(ContactActionSelector action) {
    this.action = action;
  }

  public ContactActionSelector getAction() {
    return action;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("action", action).toString();
  }
}
