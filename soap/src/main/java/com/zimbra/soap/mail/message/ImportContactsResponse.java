// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ImportContact;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_IMPORT_CONTACTS_RESPONSE)
public class ImportContactsResponse {

  /**
   * @zm-api-field-description Information about the import process
   */
  @XmlElement(name = MailConstants.E_CONTACT /* cn */, required = true)
  private ImportContact contact;

  public ImportContactsResponse() {}

  public void setContact(ImportContact contact) {
    this.contact = contact;
  }

  public ImportContact getContact() {
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
