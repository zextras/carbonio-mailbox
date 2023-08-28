// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.NewNoteSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Create a note
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_CREATE_NOTE_REQUEST)
public class CreateNoteRequest {

    /**
     * @zm-api-field-description New note specification
     */
    @XmlElement(name=MailConstants.E_NOTE, required=true)
    private final NewNoteSpec note;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CreateNoteRequest() {
        this((NewNoteSpec) null);
    }

    public CreateNoteRequest(NewNoteSpec note) {
        this.note = note;
    }

    public NewNoteSpec getNote() { return note; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("note", note)
            .toString();
    }
}
