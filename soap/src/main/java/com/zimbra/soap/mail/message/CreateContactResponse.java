// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ContactInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_CREATE_CONTACT_RESPONSE)
public class CreateContactResponse {

    /**
     * @zm-api-field-description Details of the contact.  Note that if verbose was not set in the request,
     * the returned <b>&lt;cn></b> is just a placeholder containing the new contact ID (i.e. <b>&lt;cn id="{id}"/></b>)
     */
    @XmlElement(name=MailConstants.E_CONTACT, required=false)
    private final ContactInfo contact;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CreateContactResponse() {
        this(null);
    }

    public CreateContactResponse(ContactInfo contact) {
        this.contact = contact;
    }

    public ContactInfo getContact() { return contact; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("contact", contact)
            .toString();
    }
}
