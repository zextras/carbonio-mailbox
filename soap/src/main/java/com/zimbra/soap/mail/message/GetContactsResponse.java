// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ContactInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_CONTACTS_RESPONSE)
public class GetContactsResponse {

    /**
     * @zm-api-field-description Contact information
     */
    @XmlElement(name=MailConstants.E_CONTACT /* cn */, required=false)
    private List<ContactInfo> contacts = Lists.newArrayList();

    public GetContactsResponse() {
    }

    public void setContacts(Iterable <ContactInfo> contacts) {
        this.contacts.clear();
        if (contacts != null) {
            Iterables.addAll(this.contacts,contacts);
        }
    }

    public GetContactsResponse addContact(ContactInfo contact) {
        this.contacts.add(contact);
        return this;
    }

    public List<ContactInfo> getContacts() {
        return Collections.unmodifiableList(contacts);
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("contacts", contacts);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}
