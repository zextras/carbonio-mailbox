// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ContactInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// NOTE: should we return modified attrs in response?

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_MODIFY_CONTACT_RESPONSE)
public class ModifyContactResponse {

  /**
   * @zm-api-field-description Information about modified contact
   */
  @XmlElement(name = MailConstants.E_CONTACT /* cn */, required = false)
  private final ContactInfo contact;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ModifyContactResponse() {
    this((ContactInfo) null);
  }

  public ModifyContactResponse(ContactInfo contact) {
    this.contact = contact;
  }

  public ContactInfo getContact() {
    return contact;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("contact", contact);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
